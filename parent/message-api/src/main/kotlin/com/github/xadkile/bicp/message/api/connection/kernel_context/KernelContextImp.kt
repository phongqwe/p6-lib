package com.github.xadkile.bicp.message.api.connection.kernel_context

import com.github.michaelbull.result.*
import com.github.xadkile.bicp.message.api.connection.kernel_context.context_object.*
import com.github.xadkile.bicp.message.api.connection.kernel_context.exception.*
import com.github.xadkile.bicp.message.api.connection.service.Service
import com.github.xadkile.bicp.message.api.connection.service.exception.ServiceNullException
import com.github.xadkile.bicp.message.api.connection.service.heart_beat.HeartBeatService
import com.github.xadkile.bicp.message.api.connection.service.heart_beat.coroutine.LiveCountHeartBeatServiceCoroutine
import com.github.xadkile.bicp.message.api.connection.service.iopub.IOPubListenerService
import com.github.xadkile.bicp.message.api.connection.service.iopub.IOPubListenerServiceImpl
import com.github.xadkile.bicp.message.api.connection.service.iopub.exception.IOPubListenerNotRunningException
import com.github.xadkile.bicp.message.api.exception.ExceptionInfo
import com.github.xadkile.bicp.message.api.msg.protocol.KernelConnectionFileContent
//import com.github.xadkile.bicp.message.api.connection.service.heart_beat.HeartBeatServiceUpdater
import com.github.xadkile.bicp.message.api.other.Sleeper
import com.github.xadkile.bicp.message.api.msg.protocol.other.MsgCounterImp
import com.github.xadkile.bicp.message.api.msg.protocol.other.MsgIdGenerator
import com.github.xadkile.bicp.message.api.msg.protocol.other.RandomMsgIdGenerator
import kotlinx.coroutines.*
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgCounter
import org.zeromq.ZContext
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Inject
import javax.inject.Singleton

/**
 *  [_kernelConfig] is fixed, read from an external file and only change after application start.
 */
@Singleton
class KernelContextImp @Inject internal constructor(
    private val _kernelConfig: KernelConfig,
    private val zcontext: ZContext,
    @ApplicationCScope
    private val appCScope: CoroutineScope,
    private val networkServiceDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : KernelContext {

    private val kernelTimeOut = _kernelConfig.timeOut
    // x: Context-related objects
    private var process: Process? = null
    private var connectionFileContent: KernelConnectionFileContent? = null
    private var connectionFilePath: Path? = null
    private var session: Session? = null
    private var channelProvider: ChannelProvider? = null
    private var msgEncoder: MsgEncoder? = null
    private var msgIdGenerator: MsgIdGenerator? = null
    private var msgCounter: MsgCounter? = null
    private var senderProvider: SenderProvider? = null
    private var socketProvider: SocketProvider? = null

    // x: Context-related services
    private var hbService: HeartBeatService? = null
    private var ioPubService: IOPubListenerService? = null

    // x: events listeners
    private var onBeforeStopListener: OnKernelContextEvent = OnKernelContextEvent.Nothing
    private var onAfterStopListener: OnKernelContextEvent = OnKernelContextEvent.Nothing
    private var onKernelStartedListener: OnKernelContextEvent = OnKernelContextEvent.Nothing

    private val convenientInterface = KernelContextReadOnlyConvImp(this)

    companion object {
        private val ipythonIsDownErr = KernelIsDownException.occurAt(this)
    }

    override suspend fun startAll(): Result<Unit, Exception> {
        val kernelRS: Result<Unit, Exception> = this.startKernel()
        if (kernelRS is Ok) {
            val serviceRs: Result<Unit, Exception> = this.startServices()
            return serviceRs
        } else {
            return kernelRS
        }
    }

    override suspend fun startKernel(): Result<Unit, Exception> {
        if (this.isKernelRunning()) {
            return Ok(Unit)
        } else {

            val skrs = this.startKernelProcess()
            if (skrs is Err) {
                return Err(skrs.unwrapError())
            } else {
                this.process = skrs.unwrap()
            }

            this.connectionFilePath = Paths.get(_kernelConfig.getConnectionFilePath())
            // x: wait for connection file to be written to disk by the kernel
            val waitConnectionFileWritten: Result<Unit, Exception> =
                Sleeper.delayUntil(50, kernelTimeOut.connectionFileWriteTimeout) { Files.exists(this.connectionFilePath!!) }

            if (waitConnectionFileWritten is Err) {
                return Err(CantWriteConnectionFile(ExceptionInfo(
                    msg = "Can't write connection file to disk",
                    loc = this,
                    data = this.connectionFilePath.toString()
                )))
            }

            this.connectionFileContent =
                KernelConnectionFileContent.fromJsonFile(
                    _kernelConfig.getConnectionFilePath()).unwrap()

            // x: create resources, careful with the order of resource initiation,
            // x: some must be initialized first
            // x: don't use getter here
            this.channelProvider = ChannelProviderImp(this.connectionFileContent!!)
            this.socketProvider = SocketProviderImp(this.channelProvider!!, this.zcontext)
            this.session = SessionImp.autoCreate(this.connectionFileContent?.key!!)
            this.msgEncoder = MsgEncoderImp(this.connectionFileContent?.key!!)
            this.msgCounter = MsgCounterImp()
            this.msgIdGenerator = RandomMsgIdGenerator()
            this.senderProvider = SenderProviderImp(this.conv())

            this.onKernelStartedListener.run(this)
            return Ok(Unit)
        }
    }

    private suspend fun startKernelProcess(): Result<Process, Exception> {
        val processBuilder = ProcessBuilder(this._kernelConfig.makeCompleteLaunchCmmd())
        try {
            val p: Process = processBuilder.inheritIO().start()
            val waitRs = Sleeper.delayUntil(50, kernelTimeOut.processInitTimeOut) { p.isAlive }
            if (waitRs is Err) {
                return Err(CantStartProcess(ExceptionInfo(
                    msg = "Can't start kernel process",
                    loc = this,
                    data = "kernel start command: ${this._kernelConfig.makeCompleteLaunchCmmd().joinToString(" ")}"
                )))
            }
            return Ok(p)
        } catch (e: Exception) {
            this.destroyResource()
            return Err(e)
        }
    }

    override suspend fun startServices(): Result<Unit, Exception> {
        if (this.isKernelRunning()) {

            this.hbService = LiveCountHeartBeatServiceCoroutine(
                socketProvider = this.socketProvider!!,
                zContext = this.zcontext,
                cScope = appCScope,
                cDispatcher = this.networkServiceDispatcher
            )

            val hbStartRs = this.hbService!!.start()
            if(hbStartRs is Err){
                this.hbService?.stop()
                this.hbService = null
                return hbStartRs
            }

            this.ioPubService = IOPubListenerServiceImpl(
                kernelContext = this,
                externalScope = appCScope,
                dispatcher = this.networkServiceDispatcher
            )

            val ioPubStartRs = this.ioPubService!!.start()
            if(ioPubStartRs is Err){
                this.ioPubService?.stop()
                this.ioPubService=null
                return ioPubStartRs
            }
            return Ok(Unit)

        } else {
            return Err(KernelIsDownException(ExceptionInfo(
                msg = "Can't start services because kernel is down",
                loc = this,
                data = Unit
            )))
        }
    }

    override suspend fun stopAll(): Result<Unit, Exception> {
        val r: Result<Unit, Exception> = stopServices().andThen {
            stopKernel()
        }
        return r
    }

    private suspend fun stopKernelProcess(): Result<Unit, Exception> {
        if (this.process != null) {
            this.process?.destroy()
            // x: polling until the process is completely dead
            val stopRs: Result<Unit, Exception> =
                Sleeper.delayUntil(50, kernelTimeOut.processStopTimeout) { this.process?.isAlive == false }
            val rs = stopRs.mapError {
                CantStopKernelProcess(ExceptionInfo(
                    msg = "Can't stop kernel process",
                    loc = this,
                    data = this.process?.pid()
                ))
            }
            if (rs is Err) {
                return rs
            }
            this.process = null
        }
        return Ok(Unit)
    }

    override fun getSocketProvider(): Result<SocketProvider, Exception> {
        if (this.isKernelRunning()) {
            return Ok(this.socketProvider!!)
        } else {
            return Err(ipythonIsDownErr)
        }
    }

    override suspend fun stopServices(): Result<Unit, Exception> {
        val ioPubStopRs = this.ioPubService?.stop() ?: Ok(Unit)
        if (ioPubStopRs is Err) {
            return ioPubStopRs
        }
        this.ioPubService = null

        val hbStopRs = this.hbService?.stop() ?: Ok(Unit)
        if (hbStopRs is Err) {
            return hbStopRs
        }
        this.hbService = null

        return Ok(Unit)
    }

    override suspend fun stopKernel(): Result<Unit, Exception> {
        if (this.isKernelNotRunning()) {
            return Ok(Unit)
        }
        try {
            this.onBeforeStopListener.run(this)
            val stopRs: Result<Unit, Exception> = this.stopKernelProcess()
            if (stopRs is Err) {
                return stopRs
            }
            destroyResource()
            this.onAfterStopListener.run(this)
            return Ok(Unit)
        } catch (e: Exception) {
            return Err(e)
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
        this.msgIdGenerator = null
        this.msgCounter = null
        this.senderProvider = null
        this.socketProvider = null
    }

    override fun getKernelProcess(): Result<Process, Exception> {
        if (this.isKernelRunning()) {
            return Ok(this.process!!)
        } else {
            return Err(ipythonIsDownErr)
        }
    }

    override fun getKernelInputStream(): Result<InputStream, Exception> {
        return this.getKernelProcess().map { it.inputStream }
    }

    override fun getKernelOutputStream(): Result<OutputStream, Exception> {
        return this.getKernelProcess().map { it.outputStream }
    }

    override suspend fun restartKernel(): Result<Unit, Exception> {
        if (this.isKernelRunning()) {
            val rt = this.stopAll()
                .andThen {
                    this.startAll()
                }
            return rt
        } else {
            return Err(KernelContextIllegalStateException("IPythonProcessManager is stopped, thus cannot be restarted"))
        }
    }

    override fun getConnectionFileContent(): Result<com.github.xadkile.bicp.message.api.msg.protocol.KernelConnectionFileContent, Exception> {
        if (this.isKernelRunning()) {
            return Ok(this.connectionFileContent!!)
        } else {
            return Err(ipythonIsDownErr)
        }
    }

    override fun getSession(): Result<Session, Exception> {
        if (this.isKernelRunning()) {
            return Ok(this.session!!)
        } else {
            return Err(ipythonIsDownErr)
        }
    }

    override fun getChannelProvider(): Result<ChannelProvider, Exception> {
        return this.checkKernelRunningAndGet { this.channelProvider!! }
    }

    override fun getSenderProvider(): Result<SenderProvider, Exception> {
        return this.checkKernelRunningAndGet { this.senderProvider!! }
    }

    override fun getMsgEncoder(): Result<MsgEncoder, Exception> {
        return this.checkKernelRunningAndGet(MsgEncoder::class.simpleName ?: "MsgEncoder") { this.msgEncoder!! }
    }

    override fun getMsgIdGenerator(): Result<MsgIdGenerator, Exception> {
        return this.checkKernelRunningAndGet("MsgIdGenerator") { this.msgIdGenerator!! }
    }

    private fun <T> checkKernelRunningAndGet(objectName: String = "", that: () -> T): Result<T, Exception> {
        if (this.isKernelRunning()) {
            return Ok(that())
        } else {
            return Err(KernelIsDownException(
                ExceptionInfo(
                    msg = "Can't get $objectName because kernel is down",
                    loc = this,
                    data = objectName
                )
            ))
        }
    }

    override fun isKernelRunning(): Boolean {
        val rt = this.getKernelStatus().all { it }
        return rt
    }

    override fun isServiceRunning(): Boolean {
        val hbRunning = this.hbService?.isServiceRunning() ?: false
        val ioPubRunning = this.ioPubService?.isRunning() ?: false
        return hbRunning && ioPubRunning
    }

    override fun isAllRunning(): Boolean {
        return isServiceRunning() && isKernelRunning()
    }

    override fun isKernelNotRunning(): Boolean {
        val rt = this.getKernelStatus().all { !it }
        return rt
    }

    private fun getKernelStatus(): List<Boolean> {
        val isProcessLive = this.process?.isAlive ?: false
        val isFileWritten = this.connectionFilePath?.let { Files.exists(it) } ?: false
        val connectionFileIsRead = this.connectionFileContent != null
        val isSessonOk = this.session != null
        val isChannelProviderOk = this.channelProvider != null
        val isMsgEncodeOk = this.msgEncoder != null
        val isMsgCounterOk = this.msgCounter != null
        val isSenderProviderOk = this.senderProvider != null

        val rt = listOf(
            isProcessLive, isFileWritten, connectionFileIsRead,
            isSessonOk, isChannelProviderOk, isMsgEncodeOk, isMsgCounterOk, isSenderProviderOk, /*isHBServiceRunning*/
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

    override fun getKernelConfig(): KernelConfig {
        return this._kernelConfig
    }

    override fun getIOPubListenerService(): Result<IOPubListenerService, Exception> {
//        val z = getService<IOPubListenerService>(this.ioPubService)
//        return z
        if (this.ioPubService != null) {
            if (this.ioPubService?.isRunning() == true) {
                return Ok(this.ioPubService!!)
            } else {
                return Err(IOPubListenerNotRunningException.occurAt(this))
            }
        } else {
            return Err(ServiceNullException.occurAt(this, IOPubListenerService::class.java.simpleName))
        }
    }

    override fun getHeartBeatService(): Result<HeartBeatService, Exception> {
//        val z = getService<HeartBeatService>(this.hbService)
//        return z
        if (this.hbService != null) {
            if (this.hbService?.isServiceRunning() == true) {
                return Ok(this.hbService!!)
            } else {
                return Err(IOPubListenerNotRunningException.occurAt(this))
            }
        } else {
            return Err(ServiceNullException.occurAt(this, HeartBeatService::class.java.simpleName))
        }
    }

    private fun <T> getService(service: Service?): Result<T, Exception> {
        if (service != null) {
            if (service?.isRunning() == true) {
                return Ok(service as T)
            } else {
                return Err(IOPubListenerNotRunningException.occurAt(this))
            }
        } else {
            return Err(ServiceNullException.occurAt(this,"" ))
        }
    }

    override fun conv(): KernelContextReadOnlyConv {
        return this.convenientInterface
    }

    override fun zContext(): ZContext {
        return this.zcontext
    }
}
