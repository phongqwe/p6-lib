package com.emeraldblast.p6.message.api.connection.kernel_context

import com.github.michaelbull.result.*
import com.emeraldblast.p6.common.exception.error.CommonErrors
import com.emeraldblast.p6.common.exception.error.ErrorReport
import com.emeraldblast.p6.message.api.connection.kernel_context.context_object.*
import com.emeraldblast.p6.message.api.connection.kernel_context.context_object.ChannelProvider
import com.emeraldblast.p6.message.api.connection.kernel_context.errors.KernelErrors
import com.emeraldblast.p6.message.api.message.protocol.KernelConnectionFileContent
import com.emeraldblast.p6.message.api.message.protocol.other.MsgIdGenerator
import com.emeraldblast.p6.message.api.other.Sleeper
import com.emeraldblast.p6.message.di.MsgApiCommonLogger
import kotlinx.coroutines.runBlocking
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgCounter
import org.slf4j.Logger
import org.zeromq.ZContext
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Inject

/**
 *  standard implementation of [KernelContext]
 */
class KernelContextImp @Inject internal constructor(
    // x: kernelConfig is created from an external file.
    private var iKernelConfig: KernelConfig,
    private val zContext: ZContext,
    @MsgApiCommonLogger
    private val commonLogger: Logger? = null,
    private var msgCounter: MsgCounter,
    private var msgIdGenerator: MsgIdGenerator,
    private val channelProviderFactory: ChannelProviderFactory,
    private val msgEncoderFactory: MsgEncoderFactory,
    private val socketFactoryFactory: SocketFactoryFactory,
    private val sessionFactory: SessionFactory,
    private val senderProviderFactory: SenderProviderFactory,
) : KernelContext {

    private val kernelTimeOut = kernelConfig.timeOut
    private var _isLoggerEnabled:Boolean = false
    override val isLoggerEnabled:Boolean get()=_isLoggerEnabled

    /**
     * process represent the python process
     */
    private var process: Process? = null
    private var connectionFileContent: KernelConnectionFileContent? = null
    private var connectionFilePath: Path? = null
    private var session: Session? = null

    private var channelProvider: ChannelProvider? = null
    private var msgEncoder: MsgEncoder? = null

    private var senderProvider: SenderProvider? = null
    private var socketFactory: SocketFactory? = null

    // x: events listeners
    private var onBeforeStopListener: OnKernelContextEvent = OnKernelContextEvent.Nothing
    private var onAfterStopListener: OnKernelContextEvent = OnKernelContextEvent.Nothing
    private var onKernelStartedListener: OnKernelContextEvent = OnKernelContextEvent.Nothing

    override fun enableLogger(): KernelContext {
        _isLoggerEnabled = true
        return this
    }

    override fun disableLogger(): KernelContext {
        _isLoggerEnabled = false
        return this
    }

    override fun setKernelConfig(kernelConfig: KernelConfig): KernelContext {
        if (this.isKernelRunning()) {
            throw IllegalStateException("Cannot set kernel config while the kernel is running. Stop it first.")
        }
        this.iKernelConfig = kernelConfig
        return this
    }

    override val kernelConfig: KernelConfig
        get() = iKernelConfig

    override suspend fun startAll(): Result<Unit, ErrorReport> {
        val kernelRS: Result<Unit, ErrorReport> = this.startKernel()
        return kernelRS
    }

    override suspend fun startKernel(): Result<Unit, ErrorReport> {
        if (this.isKernelRunning()) {
            return Ok(Unit)
        } else {
            val startKernelRs = this.startKernelProcess()
            when (startKernelRs) {
                is Ok -> this.process = startKernelRs.value
                is Err -> return Err(startKernelRs.error)
            }

            this.connectionFilePath = Paths.get(kernelConfig.getConnectionFilePath())

            // x: wait for connection file to be written to disk by the kernel
            // x: TODO this can be improved using watch service of nio
            val waitConnectionFileWritten: Result<Unit, ErrorReport> =
                Sleeper.delayUntil(
                    50,
                    kernelTimeOut.connectionFileWriteTimeout
                ) { Files.exists(this.connectionFilePath!!) }

            if (waitConnectionFileWritten is Err) {
                val report = ErrorReport(
                    KernelErrors.CantWriteConnectionFile.header,
                    KernelErrors.CantWriteConnectionFile.Data(this.connectionFilePath),
                )
                return Err(report)
            }

            this.connectionFileContent = this.kernelConfig.kernelConnectionFileContent
            val connectionFiles = this.connectionFileContent!!

            // x: create resources, careful with the order of resource initiation,
            // x: some must be initialized first
            // x: must NOT use getters here because getters always check for kernel status before return derivative objects
            this.channelProvider = channelProviderFactory.create(connectionFiles)
            this.socketFactory = socketFactoryFactory.create(this.channelProvider!!, this.zContext)
            this.session = sessionFactory.create(connectionFiles.key)
            this.msgEncoder = msgEncoderFactory.create(connectionFiles.key)
            this.senderProvider = senderProviderFactory.create()
            this.onKernelStartedListener.run(this)
            Runtime.getRuntime().addShutdownHook(Thread {
                // x: kill kernel context when jvm stops
                runBlocking {
                    stopAll()
                }
            })
            return Ok(Unit)
        }
    }


    private suspend fun startKernelProcess(): Result<Process, ErrorReport> {
        val processBuilder = ProcessBuilder(this.kernelConfig.makeCompleteLaunchCmmd())
        try {
            val p: Process = processBuilder.inheritIO().start()
            val waitForProcessRs = Sleeper.delayUntil(50, kernelTimeOut.processInitTimeOut) { p.isAlive }
            if (waitForProcessRs is Err) {
                return KernelErrors.CantStartKernelProcess
                    .report(this.kernelConfig.makeCompleteLaunchCmmd().joinToString(" "))
                    .toErr()
            }
            return Ok(p)
        } catch (e: Exception) {
            this.destroyResource()
            val report = ErrorReport(
                header = CommonErrors.Unknown.header,
                data = CommonErrors.Unknown.Data("calling KernelContextImp.startKernelProcess()", e)
            )
            return Err(report)
        }
    }

    override suspend fun stopAll(): Result<Unit, ErrorReport> {
        return stopKernel()
    }

    private suspend fun stopKernelProcess(): Result<Unit, ErrorReport> {
        if (this.process != null) {
            this.process?.destroyForcibly()
            // x: polling until the process is completely dead
            val waitForProcessStopRs: Result<Unit, ErrorReport> =
                Sleeper.delayUntil(50, kernelTimeOut.processStopTimeout) { this.process?.isAlive == false }
            val rs = waitForProcessStopRs.mapError {
                ErrorReport(
                    header = KernelErrors.CantStopKernelProcess.header,
                    data = KernelErrors.CantStopKernelProcess.Data(this.process?.pid())
                )
            }
            if (rs is Err) {
                return rs
            }
            this.process = null
        }
        return Ok(Unit)
    }


    override fun getSocketProvider(): Result<SocketFactory, ErrorReport> {
        if (this.isKernelRunning()) {
            return Ok(this.socketFactory!!)
        } else {
            return Err(KernelErrors.KernelDown.report("Can't get socket provider because the kernel is down"))
        }
    }



    override suspend fun stopKernel(): Result<Unit, ErrorReport> {
        if (this.isKernelNotRunning()) {
            return Ok(Unit)
        }
        try {
            this.onBeforeStopListener.run(this)
            val stopRs: Result<Unit, ErrorReport> = this.stopKernelProcess()
            if (stopRs is Err) {
                return stopRs
            }
            destroyResource()
            this.onAfterStopListener.run(this)
            return Ok(Unit)
        } catch (e: Exception) {
            val report = ErrorReport(
                header = CommonErrors.Unknown.header,
                data = CommonErrors.Unknown.Data("calling KernelContextImp.stopKernel()", e)
            )
            return Err(report)
        }
    }

    private suspend fun destroyResource() {
        val cpath = this.connectionFilePath

        if (cpath != null) {
            // x: delete connection file
            Files.delete(cpath)
            // rmd: wait until file is deleted completely
            Sleeper.delayUntil(50) { !Files.exists(cpath) }
            this.connectionFilePath = null
        }
        // x: destroy other resources
        this.connectionFileContent = null
        this.session = null
        this.channelProvider = null
        this.msgEncoder = null
        this.msgCounter.reset()
        this.senderProvider = null
        this.socketFactory = null
    }

    override fun getKernelProcess(): Result<Process, ErrorReport> {
        if (this.process != null && this.process?.isAlive == true) {
            return Ok(this.process!!)
        } else {
            return Err(KernelErrors.KernelDown.report("Can't get kernel process because the kernel is down"))
        }
    }

    override fun getKernelInputStream(): Result<InputStream, ErrorReport> {
        return this.getKernelProcess().map { it.inputStream }
    }

    override fun getKernelOutputStream(): Result<OutputStream, ErrorReport> {
        return this.getKernelProcess().map { it.outputStream }
    }

    override suspend fun restartKernel(kernelConfig: KernelConfig?): Result<Unit, ErrorReport> {
        if(kernelConfig!=null){
            this.iKernelConfig = kernelConfig
        }
        if (this.isKernelRunning()) {
            val rt = this.stopAll()
                .andThen {
                    this.startAll()
                }
            return rt
        } else {
            val report = ErrorReport(
                header = KernelErrors.KernelContextIllegalState.header,
                data = KernelErrors.KernelContextIllegalState.Data(
                    currentState = "not running",
                    actionToPerform = "restart kernel"
                )
            )
            return Err(report)
        }
    }


    override fun getConnectionFileContent(): Result<KernelConnectionFileContent, ErrorReport> {
        if (this.isKernelRunning()) {
            return Ok(this.connectionFileContent!!)
        } else {
            return Err(KernelErrors.KernelDown.report("Can't get connection file content because the kernel is down"))
        }
    }


    override fun getSession(): Result<Session, ErrorReport> {
        if (this.isKernelRunning()) {
            return Ok(this.session!!)
        } else {
            return Err(KernelErrors.KernelDown.report("Can't get session object because the kernel is down"))
        }
    }


    override fun getChannelProvider(): Result<ChannelProvider, ErrorReport> {
        return this.checkKernelRunningAndGet2 { this.channelProvider!! }
    }


    override fun getSenderProvider(): Result<SenderProvider, ErrorReport> {
        return this.checkKernelRunningAndGet2 { this.senderProvider!! }
    }


    override fun getMsgEncoder(): Result<MsgEncoder, ErrorReport> {
        return this.checkKernelRunningAndGet2(MsgEncoder::class.simpleName ?: "MsgEncoder") { this.msgEncoder!! }
    }


    override fun getMsgIdGenerator(): Result<MsgIdGenerator, ErrorReport> {
        return Ok(this.msgIdGenerator)
    }

    private fun <T> checkKernelRunningAndGet2(objectName: String = "", that: () -> T): Result<T, ErrorReport> {
        if (this.isKernelRunning()) {
            return Ok(that())
        } else {
            val report = ErrorReport(
                header = KernelErrors.GetKernelObjectError.header,
                data = KernelErrors.GetKernelObjectError.Data(objectName),
            )
            return Err(report)
        }
    }

    override fun isKernelRunning(): Boolean {
        val rt = this.kernelStatus.isOk()
        return rt
    }



    override fun isAllRunning(): Boolean {
        return  isKernelRunning()
    }

    override fun isKernelNotRunning(): Boolean {
        return !this.isKernelRunning()
    }

    /**
     * Kernel status does NOT include service status.
     */
    override val kernelStatus: KernelStatus
        get() {
            val isProcessLive = this.process?.isAlive ?: false
            val isConnectionFileWritten = this.connectionFilePath?.let { Files.exists(it) } ?: false
            val connectionFileIsRead = this.connectionFileContent != null
            val isSessonOk = this.session != null
            val isChannelProviderOk = this.channelProvider != null
            val isMsgEncodeOk = this.msgEncoder != null
            val isSenderProviderOk = this.senderProvider != null

            val rt = KernelStatus(
                isProcessLive = isProcessLive,
                isConnectionFileWritten = isConnectionFileWritten,
                connectionFileIsRead = connectionFileIsRead,
                isSessonOk = isSessonOk,
                isChannelProviderOk = isChannelProviderOk,
                isMsgEncodeOk = isMsgEncodeOk,
                isSenderProviderOk = isSenderProviderOk,
            )
            return rt
        }

    override fun setOnBeforeStopListener(listener: OnKernelContextEvent) {
        this.onBeforeStopListener = listener
    }

    override fun removeBeforeStopListener() {
        this.onBeforeStopListener = OnKernelContextEvent.Nothing
    }

    override fun setOnAfterStopListener(listener: OnKernelContextEvent) {
        this.onAfterStopListener = listener
    }

    override fun removeAfterStopListener() {
        this.onAfterStopListener = OnKernelContextEvent.Nothing
    }

    override fun setKernelStartedListener(listener: OnKernelContextEvent) {
        this.onKernelStartedListener = listener
    }

    override fun removeOnProcessStartListener() {
        this.onKernelStartedListener = OnKernelContextEvent.Nothing
    }

    override fun zContext(): ZContext {
        return this.zContext
    }
}
