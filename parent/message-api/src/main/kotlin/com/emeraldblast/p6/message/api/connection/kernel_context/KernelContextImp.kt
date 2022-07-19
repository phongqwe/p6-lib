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
    private var iKernelConfig: KernelConfig?,
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

    private val kernelTimeOut get() = kernelConfig?.timeOut
    private var _isLoggerEnabled: Boolean = false
    override val isLoggerEnabled: Boolean get() = _isLoggerEnabled
    private var deleteConnectionFile = false

    /**
     * process represent the python process
     */
    private var process: Process? = null
    private var processIsUnderManagement = false
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
        this.clearConfig()
        this.iKernelConfig = kernelConfig
        deleteConnectionFile = true
        return this
    }

    override fun setConnectionFileContent(connectionFileContent: KernelConnectionFileContent): KernelContext {
        if (this.isKernelRunning()) {
            throw IllegalStateException("Cannot set connection file content while the kernel is running. Stop it first.")
        }
        this.clearConfig()
        this.connectionFileContent = connectionFileContent
        deleteConnectionFile = false
        return this
    }

    override fun setConnectionFilePath(connectionFilePath: Path): KernelContext {
        if (this.isKernelRunning()) {
            throw IllegalStateException("Cannot set connection file path while the kernel is running. Stop it first.")
        }
        this.clearConfig()
        this.connectionFilePath = connectionFilePath
        deleteConnectionFile = false
        return this
    }

    override val kernelConfig: KernelConfig?
        get() = iKernelConfig

    override fun startAll(): Result<Unit, ErrorReport> {
        val kernelRS: Result<Unit, ErrorReport> = this.startKernel()
        return kernelRS
    }

    override fun startKernel(): Result<Unit, ErrorReport> {
        if (this.isKernelRunning()) {
            return Ok(Unit)
        } else {
            val kernelConfig = this.kernelConfig
            if (kernelConfig == null && connectionFileContent == null && connectionFilePath == null) {
                return KernelErrors.CantStartKernelContext
                    .report(
                        "Can't start kernel because kernel config and connection file are not available. At least one of these must be provided:\n" +
                                "- kernel config object\n" +
                                "- connection file path\n" +
                                "- connection file content"
                    )
                    .toErr()
            } else {
                if (kernelConfig != null) {
                    val kernelTimeOut = kernelConfig.timeOut
                    when (val startKernelRs = this.startKernelProcess()) {
                        is Ok -> this.process = startKernelRs.value
                        is Err -> {
                            this.stopAll()
                            return Err(startKernelRs.error)
                        }
                    }
                    this.connectionFilePath = Paths.get(kernelConfig.connectionFilePath)
                    // x: wait for connection file to be written to disk by the kernel
                    val waitConnectionFileWritten: Result<Unit, ErrorReport> =
                        Sleeper.waitBlockUntil(
                            50,
                            kernelTimeOut.connectionFileWriteTimeout
                        ) { Files.exists(this.connectionFilePath!!) }

                    if (waitConnectionFileWritten is Err) {
                        this.stopAll()
                        return KernelErrors.CantWriteConnectionFile
                            .report("Timeout while waiting (${kernelTimeOut.connectionFileWriteTimeout} milli seconds) for connection file to be written to disk at ${this.connectionFilePath}.")
                            .toErr()
                    }
                    this.connectionFileContent = kernelConfig.kernelConnectionFileContent
                } else if (this.connectionFilePath != null) {
                    processIsUnderManagement = false
                    this.connectionFileContent =
                        KernelConnectionFileContent.fromJsonFile2(this.connectionFilePath!!).getOr(null)
                } else if (this.connectionFileContent != null) {
                    processIsUnderManagement = false
                }

                val connectionFiles = this.connectionFileContent
                if (connectionFiles != null) {
                    // x: create resources, careful with the order of resource initiation,
                    // x: some must be initialized first
                    // x: must NOT use getters here because getters always check for kernel status before return resource objects
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
                } else {
                    stopAll()
                    return KernelErrors.CantStartKernelContext
                        .report("Can't start kernel context because connection file content is not available.")
                        .toErr()
                }
            }
        }
    }

    override fun startKernel(kernelConfig: KernelConfig): Result<Unit, ErrorReport> {
        if (this.isKernelRunning()) {
            return Ok(Unit)
        } else {
            this.clearConfig()
            this.setKernelConfig(kernelConfig)
            return this.startKernel()
        }
    }

    override fun startKernel(connectionFilePath: Path): Result<Unit, ErrorReport> {
        if (this.isKernelRunning()) {
            return Ok(Unit)
        } else {
            this.clearConfig()
            this.setConnectionFilePath(connectionFilePath)
            return this.startKernel()
        }
    }

    override fun startKernel(connectionFileContent: KernelConnectionFileContent): Result<Unit, ErrorReport> {
        if (this.isKernelRunning()) {
            return Ok(Unit)
        } else {
            this.clearConfig()
            this.setConnectionFileContent(connectionFileContent)
            return this.startKernel()
        }
    }

    private fun startKernelProcess(): Result<Process, ErrorReport> {
        val kernelConfig = this.kernelConfig
        if (kernelConfig != null) {
            val processBuilder = ProcessBuilder(kernelConfig.makeCompleteLaunchCmd())
            try {
                val p: Process = processBuilder.inheritIO().start()
                val waitForProcessRs = Sleeper.waitBlockUntil(50, kernelConfig.timeOut.processInitTimeOut) { p.isAlive }
                if (waitForProcessRs is Err) {
                    val command = kernelConfig.makeCompleteLaunchCmd().joinToString(" ")
                    return KernelErrors.CantStartKernelProcess
                        .reportForCommand(command)
                        .toErr()
                }
                processIsUnderManagement = true
                return Ok(p)
            } catch (e: Throwable) {
                this.destroyResource()
                return CommonErrors.ExceptionError
                    .report("Encounter exception when trying to start kernel process", e)
                    .toErr()
            }
        } else {
            return KernelErrors.KernelConfigIsNull.report("Can't start kernel because kernel config is not available")
                .toErr()
        }
    }

    override fun stopAll(): Result<Unit, ErrorReport> {
        return stopKernel()
    }

    private fun stopKernelProcess(): Result<Unit, ErrorReport> {
        if (this.process != null) {
            this.process?.destroyForcibly()
            val waitConnectionFileWritten: Result<Unit, ErrorReport> =
                Sleeper.waitBlockUntil(
                    50,
                    kernelTimeOut?.connectionFileWriteTimeout ?: KernelTimeOut.defaultTimeOut
                ) { this.process?.isAlive == false }
            if (waitConnectionFileWritten is Err) {
                return waitConnectionFileWritten
            }
            this.process = null
        }
        return Ok(Unit)
    }

    override fun getSocketFactory(): Result<SocketFactory, ErrorReport> {
        if (this.isKernelRunning()) {
            return Ok(this.socketFactory!!)
        } else {
            return Err(KernelErrors.KernelDown.report("Can't get socket provider because the kernel is down"))
        }
    }

    override fun stopKernel(): Result<Unit, ErrorReport> {
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
        } catch (e: Throwable) {
            return CommonErrors.ExceptionError
                .report("Encounter exception when trying to stop kernel", e)
                .toErr()
        }
    }

    override fun restartKernel(): Result<Unit, ErrorReport> {
        if (this.isKernelRunning()) {
            val rt = this.stopAll()
                .andThen {
                    this.startKernel()
                }
            return rt
        } else {
            val report = ErrorReport(
                header = KernelErrors.KernelContextIllegalState.header.setDescription("Can't restart kernel because it is not running."),
            )
            return Err(report)
        }
    }

    private fun destroyResource() {
        val cpath = this.connectionFilePath

        if (cpath != null && this.deleteConnectionFile) {
            // x: delete connection file
            Files.delete(cpath)
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
        if (this.process != null) {
            return Ok(this.process!!)
        } else {
            return Err(KernelErrors.KernelProcessIsNotAvailable.report())
        }
    }

    override fun getKernelInputStream(): Result<InputStream, ErrorReport> {
        return this.getKernelProcess().map { it.inputStream }
    }

    override fun getKernelOutputStream(): Result<OutputStream, ErrorReport> {
        return this.getKernelProcess().map { it.outputStream }
    }


    override fun restartKernel(kernelConfig: KernelConfig): Result<Unit, ErrorReport> {
        if (this.isKernelRunning()) {
            val rt = this.stopAll()
                .andThen {
                    this.clearConfig()
                    this.setKernelConfig(kernelConfig)
                    this.startKernel()
                }
            return rt
        } else {
            val report = ErrorReport(
                header = KernelErrors.KernelContextIllegalState.header.setDescription("Can't restart kernel because it is not running."),
            )
            return Err(report)
        }
    }

    override fun restartKernel(connectionFilePath: Path): Result<Unit, ErrorReport> {
        if (this.isKernelRunning()) {
            val rt = this.stopAll()
                .andThen {
                    this.clearConfig()
                    this.setConnectionFilePath(connectionFilePath)
                    this.startKernel()
                }
            return rt
        } else {
            val report = ErrorReport(
                header = KernelErrors.KernelContextIllegalState.header.setDescription("Can't restart kernel because it is not running."),
            )
            return Err(report)
        }
    }

    override fun restartKernel(connectionFileContent: KernelConnectionFileContent): Result<Unit, ErrorReport> {
        if (this.isKernelRunning()) {
            val rt = this.stopAll()
                .andThen {
                    this.clearConfig()
                    this.setConnectionFileContent(connectionFileContent)
                    this.startKernel()
                }
            return rt
        } else {
            val report = ErrorReport(
                header = KernelErrors.KernelContextIllegalState.header.setDescription("Can't restart kernel because it is not running."),
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
            return KernelErrors.KernelDown
                .report("Can't get ${objectName} because the kernel is down.")
                .toErr()
        }
    }

    override fun isKernelRunning(): Boolean {
        val rt = this.kernelStatus.isOk()
        return rt
    }

    override fun isAllRunning(): Boolean {
        return isKernelRunning()
    }

    override fun isKernelNotRunning(): Boolean {
        return !this.isKernelRunning()
    }

    private fun clearConfig(): KernelContext {
        if (this.isKernelRunning()) {
            throw IllegalStateException("Cannot clear config while the kernel is running. Stop it first.")
        }
        this.iKernelConfig = null
        this.connectionFileContent = null
        this.connectionFilePath = null
        processIsUnderManagement = false
        return this
    }

    /**
     * Kernel status does NOT include service status.
     */
    override val kernelStatus: KernelStatus
        get() {
            val isProcessLive = run {
                if (this.processIsUnderManagement) {
                    this.process?.isAlive ?: false
                } else {
                    true
                }
            }
//            val isConnectionFileIsAvailable = (this.connectionFilePath?.let { Files.exists(it) } ?: false)
            val isConnectionFileIsAvailable = this.connectionFileContent != null
            val connectionFileIsRead = this.connectionFileContent != null
            val isSessonOk = this.session != null
            val isChannelProviderOk = this.channelProvider != null
            val isMsgEncodeOk = this.msgEncoder != null
            val isSenderProviderOk = this.senderProvider != null

            val rt = KernelStatus(
                isProcessLive = isProcessLive,
                isConnectionFileWritten = isConnectionFileIsAvailable,
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
