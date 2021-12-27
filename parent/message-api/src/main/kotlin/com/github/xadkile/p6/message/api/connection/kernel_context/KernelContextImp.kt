package com.github.xadkile.p6.message.api.connection.kernel_context

import com.github.michaelbull.result.*
import com.github.xadkile.p6.message.api.connection.kernel_context.context_object.*
import com.github.xadkile.p6.message.api.connection.kernel_context.exception.*
import com.github.xadkile.p6.message.api.connection.service.Service
import com.github.xadkile.p6.message.api.connection.service.exception.ServiceNullException
import com.github.xadkile.p6.message.api.connection.service.heart_beat.HeartBeatService
import com.github.xadkile.p6.message.api.connection.service.heart_beat.coroutine.LiveCountHeartBeatServiceCoroutine
import com.github.xadkile.p6.message.api.connection.service.iopub.IOPubListenerService
import com.github.xadkile.p6.message.api.connection.service.iopub.IOPubListenerServiceImpl
import com.github.xadkile.p6.message.api.connection.service.iopub.exception.IOPubListenerNotRunningException
import com.github.xadkile.p6.exception.ExceptionInfo
import com.github.xadkile.p6.exception.error.CommonErrors
import com.github.xadkile.p6.exception.error.ErrorReport
import com.github.xadkile.p6.message.api.connection.service.exception.ServiceErrors
import com.github.xadkile.p6.message.api.connection.service.iopub.IOPubListenerServiceReadOnly
import com.github.xadkile.p6.message.api.connection.service.iopub.exception.IOPubServiceErrors
import com.github.xadkile.p6.message.api.msg.protocol.KernelConnectionFileContent
//import com.github.xadkile.p6.message.api.connection.service.heart_beat.HeartBeatServiceUpdater
import com.github.xadkile.p6.message.api.other.Sleeper
import com.github.xadkile.p6.message.api.msg.protocol.other.MsgCounterImp
import com.github.xadkile.p6.message.api.msg.protocol.other.MsgIdGenerator
import com.github.xadkile.p6.message.api.msg.protocol.other.RandomMsgIdGenerator
import kotlinx.coroutines.*
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgCounter
import org.zeromq.ZContext
import zmq.io.StreamEngine
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Inject
import javax.inject.Singleton

/**
 *  standard implementation of [KernelContext]
 */
@Singleton
class KernelContextImp @Inject internal constructor(
    // x: _kernelConfig is created from an external file.
    private val kernelConfig: KernelConfig,
    private val zcontext: ZContext,
    @ApplicationCScope
    private val appCScope: CoroutineScope,
    // x: dispatcher (a group of threads) on which network communication services run on
    private val networkServiceDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : KernelContext {

    private val kernelTimeOut = kernelConfig.timeOut

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

    private val convInstance = KernelContextReadOnlyConvImp(this)

    companion object {
        private val ipythonIsDownErr = KernelIsDownException.occurAt(this)
        private val kernelDownReport = ErrorReport(
            header = KernelErrors.KernelDown,
            data = KernelErrors.KernelDown.Data(""),
            loc = "${this.javaClass.canonicalName}:getSession",
        )
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

    override suspend fun startAll2(): Result<Unit, ErrorReport> {
        val kernelRS: Result<Unit, ErrorReport> = this.startKernel2()
        if (kernelRS is Ok) {
            val serviceRs: Result<Unit, ErrorReport> = this.startServices2()
            return serviceRs
        } else {
            return kernelRS
        }
    }


    @Deprecated("replace with ErrorReport api")
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

            this.connectionFilePath = Paths.get(kernelConfig.getConnectionFilePath())
            // x: wait for connection file to be written to disk by the kernel
            val waitConnectionFileWritten: Result<Unit, Exception> =
                Sleeper.delayUntil(50,
                    kernelTimeOut.connectionFileWriteTimeout) { Files.exists(this.connectionFilePath!!) }

            if (waitConnectionFileWritten is Err) {
                return Err(CantWriteConnectionFile(ExceptionInfo(
                    msg = "Can't write connection file to disk",
                    loc = this,
                    data = this.connectionFilePath.toString()
                )))
            }

            this.connectionFileContent =
                KernelConnectionFileContent.fromJsonFile(
                    kernelConfig.getConnectionFilePath()).unwrap()

            // x: create resources, careful with the order of resource initiation,
            // x: some must be initialized first
            // x: must NOT use getters here because getters always check for kernel status before return derivative objects
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

    override suspend fun startKernel2(): Result<Unit, ErrorReport> {
        if (this.isKernelRunning()) {
            return Ok(Unit)
        } else {

            val skrs = this.startKernelProcess2()
            if (skrs is Err) {
                return Err(skrs.unwrapError())
            } else {
                this.process = skrs.unwrap()
            }

            this.connectionFilePath = Paths.get(kernelConfig.getConnectionFilePath())
            // x: wait for connection file to be written to disk by the kernel
            val waitConnectionFileWritten: Result<Unit, Exception> =
                Sleeper.delayUntil(50,
                    kernelTimeOut.connectionFileWriteTimeout) { Files.exists(this.connectionFilePath!!) }

            if (waitConnectionFileWritten is Err) {
                val report = ErrorReport(KernelErrors.CantWriteConnectionFile,
                    KernelErrors.CantWriteConnectionFile.Data(this.connectionFilePath),
                    "")
                return Err(report)
            }

            this.connectionFileContent =
                KernelConnectionFileContent.fromJsonFile(
                    kernelConfig.getConnectionFilePath()).unwrap()

            // x: create resources, careful with the order of resource initiation,
            // x: some must be initialized first
            // x: must NOT use getters here because getters always check for kernel status before return derivative objects
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

    @Deprecated("replace with ErrorReport api")
    private suspend fun startKernelProcess(): Result<Process, Exception> {
        val processBuilder = ProcessBuilder(this.kernelConfig.makeCompleteLaunchCmmd())
        try {
            val p: Process = processBuilder.inheritIO().start()
            val waitRs = Sleeper.delayUntil(50, kernelTimeOut.processInitTimeOut) { p.isAlive }
            if (waitRs is Err) {
                return Err(CantStartProcess(ExceptionInfo(
                    msg = "Can't start kernel process",
                    loc = this,
                    data = "kernel start command: ${this.kernelConfig.makeCompleteLaunchCmmd().joinToString(" ")}"
                )))
            }
            return Ok(p)
        } catch (e: Exception) {
            this.destroyResource()
            return Err(e)
        }
    }

    private suspend fun startKernelProcess2(): Result<Process, ErrorReport> {
        val processBuilder = ProcessBuilder(this.kernelConfig.makeCompleteLaunchCmmd())
        try {
            val p: Process = processBuilder.inheritIO().start()
            val waitRs = Sleeper.delayUntil(50, kernelTimeOut.processInitTimeOut) { p.isAlive }
            if (waitRs is Err) {
                val report = ErrorReport(
                    KernelErrors.CantStartProcess,
                    KernelErrors.CantStartProcess.Data(this.kernelConfig.makeCompleteLaunchCmmd().joinToString(" ")))
                return Err(report)
            }
            return Ok(p)
        } catch (e: Exception) {
            this.destroyResource()
            val report = ErrorReport(
                header = CommonErrors.Unknown,
                data = CommonErrors.Unknown.Data("calling KernelContextImp.startKernelProcess()", e)
            )
            return Err(report)
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
            if (hbStartRs is Err) {
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
            if (ioPubStartRs is Err) {
                this.ioPubService?.stop()
                this.ioPubService = null
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

    override suspend fun startServices2(): Result<Unit, ErrorReport> {
        if (this.isKernelRunning()) {

            this.hbService = LiveCountHeartBeatServiceCoroutine(
                socketProvider = this.socketProvider!!,
                zContext = this.zcontext,
                cScope = appCScope,
                cDispatcher = this.networkServiceDispatcher
            )

            val hbStartRs = this.hbService!!.start2()
            if (hbStartRs is Err) {
                this.hbService?.stop()
                this.hbService = null
                return hbStartRs
            }

            this.ioPubService = IOPubListenerServiceImpl(
                kernelContext = this,
                externalScope = appCScope,
                dispatcher = this.networkServiceDispatcher
            )

            val ioPubStartRs = this.ioPubService!!.start2()
            if (ioPubStartRs is Err) {
                this.ioPubService?.stop()
                this.ioPubService = null
                return ioPubStartRs
            }
            return Ok(Unit)

        } else {
            val report = ErrorReport(
                header = KernelErrors.KernelDown,
                data = KernelErrors.KernelDown.Data(this::class.java.canonicalName)
            )
            return Err(report)
        }
    }

    override suspend fun stopAll(): Result<Unit, Exception> {
        val r: Result<Unit, Exception> = stopServices().andThen {
            stopKernel()
        }
        return r
    }

    override suspend fun stopAll2(): Result<Unit, ErrorReport> {
        val r: Result<Unit, ErrorReport> = stopServices2().andThen {
            stopKernel2()
        }
        return r
    }

    @Deprecated("replace with ErrorReport api")
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

    private suspend fun stopKernelProcess2(): Result<Unit, ErrorReport> {
        if (this.process != null) {
            this.process?.destroy()
            // x: polling until the process is completely dead
            val stopRs: Result<Unit, Exception> =
                Sleeper.delayUntil(50, kernelTimeOut.processStopTimeout) { this.process?.isAlive == false }
            val rs = stopRs.mapError {
                ErrorReport(
                    header = KernelErrors.CantStopKernelProcess,
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


    override fun getSocketProvider(): Result<SocketProvider, Exception> {
        if (this.isKernelRunning()) {
            return Ok(this.socketProvider!!)
        } else {
            return Err(ipythonIsDownErr)
        }
    }

    override fun getSocketProvider2(): Result<SocketProvider, ErrorReport> {
        if (this.isKernelRunning()) {
            return Ok(this.socketProvider!!)
        } else {
            return Err(kernelDownReport)
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

    override suspend fun stopServices2(): Result<Unit, ErrorReport> {
        val ioPubStopRs = this.ioPubService?.stop2() ?: Ok(Unit)
        if (ioPubStopRs is Err) {
            return ioPubStopRs
        }
        this.ioPubService = null

        val hbStopRs = this.hbService?.stop2() ?: Ok(Unit)
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

    override suspend fun stopKernel2(): Result<Unit, ErrorReport> {
        if (this.isKernelNotRunning()) {
            return Ok(Unit)
        }
        try {
            this.onBeforeStopListener.run(this)
            val stopRs: Result<Unit, ErrorReport> = this.stopKernelProcess2()
            if (stopRs is Err) {
                return stopRs
            }
            destroyResource()
            this.onAfterStopListener.run(this)
            return Ok(Unit)
        } catch (e: Exception) {
            val report = ErrorReport(
                header = CommonErrors.Unknown,
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

    override fun getKernelProcess2(): Result<Process, ErrorReport> {
        if (this.isKernelRunning()) {
            return Ok(this.process!!)
        } else {
            val report = ErrorReport(
                header = KernelErrors.KernelDown,
                data = KernelErrors.KernelDown.Data("getKernelProcess")
            )
            return Err(report)
        }
    }

    override fun getKernelInputStream(): Result<InputStream, Exception> {
        return this.getKernelProcess().map { it.inputStream }
    }

    override fun getKernelInputStream2(): Result<InputStream, ErrorReport> {
        return this.getKernelProcess2().map { it.inputStream }
    }

    override fun getKernelOutputStream(): Result<OutputStream, Exception> {
        return this.getKernelProcess().map { it.outputStream }
    }

    override fun getKernelOutputStream2(): Result<OutputStream, ErrorReport> {
        return this.getKernelProcess2().map { it.outputStream }
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

    override suspend fun restartKernel2(): Result<Unit, ErrorReport> {
        if (this.isKernelRunning()) {
            val rt = this.stopAll2()
                .andThen {
                    this.startAll2()
                }
            return rt
        } else {
            val report = ErrorReport(
                header = KernelErrors.KernelContextIllegalState,
                data = KernelErrors.KernelContextIllegalState.Data(
                    currentState = "not running",
                    actionToPerform = "restart kernel"
                )
            )
            return Err(report)
        }
    }

    override fun getConnectionFileContent(): Result<KernelConnectionFileContent, Exception> {
        if (this.isKernelRunning()) {
            return Ok(this.connectionFileContent!!)
        } else {
            return Err(ipythonIsDownErr)
        }
    }

    override fun getConnectionFileContent2(): Result<KernelConnectionFileContent, ErrorReport> {
        if (this.isKernelRunning()) {
            return Ok(this.connectionFileContent!!)
        } else {
            return Err(kernelDownReport)
        }
    }

    override fun getSession(): Result<Session, Exception> {
        if (this.isKernelRunning()) {
            return Ok(this.session!!)
        } else {
            return Err(ipythonIsDownErr)
        }
    }

    override fun getSession2(): Result<Session, ErrorReport> {
        if (this.isKernelRunning()) {
            return Ok(this.session!!)
        } else {
            return Err(kernelDownReport)
        }
    }

    override fun getChannelProvider(): Result<ChannelProvider, Exception> {
        return this.checkKernelRunningAndGet { this.channelProvider!! }
    }

    override fun getChannelProvider2(): Result<ChannelProvider, ErrorReport> {
        return this.checkKernelRunningAndGet2 { this.channelProvider!! }
    }

    override fun getSenderProvider(): Result<SenderProvider, Exception> {
        return this.checkKernelRunningAndGet { this.senderProvider!! }
    }

    override fun getSenderProvider2(): Result<SenderProvider, ErrorReport> {
        return this.checkKernelRunningAndGet2 { this.senderProvider!! }
    }

    override fun getMsgEncoder(): Result<MsgEncoder, Exception> {
        return this.checkKernelRunningAndGet(MsgEncoder::class.simpleName ?: "MsgEncoder") { this.msgEncoder!! }
    }

    override fun getMsgEncoder2(): Result<MsgEncoder, ErrorReport> {
        return this.checkKernelRunningAndGet2(MsgEncoder::class.simpleName ?: "MsgEncoder") { this.msgEncoder!! }
    }

    override fun getMsgIdGenerator(): Result<MsgIdGenerator, Exception> {
        return this.checkKernelRunningAndGet("MsgIdGenerator") { this.msgIdGenerator!! }
    }

    override fun getMsgIdGenerator2(): Result<MsgIdGenerator, ErrorReport> {
        return this.checkKernelRunningAndGet2("MsgIdGenerator") { this.msgIdGenerator!! }
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

    private fun <T> checkKernelRunningAndGet2(objectName: String = "", that: () -> T): Result<T, ErrorReport> {
        if (this.isKernelRunning()) {
            return Ok(that())
        } else {
            val report = ErrorReport(
                header = KernelErrors.GetKernelObjectError,
                data = KernelErrors.GetKernelObjectError.Data(objectName),
                loc = "${this.javaClass.canonicalName}:checkKernelRunningAndGet"
            )
            return Err(report)
        }
    }

    override fun isKernelRunning(): Boolean {
        val rt = this.getKernelStatus().all { it }
        return rt
    }

    override fun areServicesRunning(): Boolean {
        val hbRunning = this.hbService?.isServiceRunning() ?: false
        val ioPubRunning = this.ioPubService?.isRunning() ?: false
        return hbRunning && ioPubRunning
    }

    override fun isAllRunning(): Boolean {
        return areServicesRunning() && isKernelRunning()
    }

    override fun isKernelNotRunning(): Boolean {
        return !this.isKernelRunning()
    }

    /**
     * Kernel status does NOT include service status
     */
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
            isSessonOk, isChannelProviderOk, isMsgEncodeOk, isMsgCounterOk, isSenderProviderOk,
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
        return this.kernelConfig
    }

    override fun getIOPubListenerService(): Result<IOPubListenerService, Exception> {
        val sv = getService<IOPubListenerService>(this.ioPubService)
        return sv
    }

    override fun getIOPubListenerService2(): Result<IOPubListenerServiceReadOnly, ErrorReport> {
        val sv = getService2<IOPubListenerService>(this.ioPubService, "IO Pub service")
        return sv
    }

    override fun getHeartBeatService(): Result<HeartBeatService, Exception> {
        val sv = getService<HeartBeatService>(this.hbService)
        return sv
    }

    override fun getHeartBeatService2(): Result<HeartBeatService, ErrorReport> {
        val sv = getService2<HeartBeatService>(this.hbService, "heart beat service")
        return sv
    }

    private fun <T> getService(service: Service?): Result<T, Exception> {
        if (service != null) {
            if (service.isRunning() == true) {
                return Ok(service as T)
            } else {
                return Err(IOPubListenerNotRunningException.occurAt(this))
            }
        } else {
            return Err(ServiceNullException.occurAt(this, ""))
        }
    }

    private fun <T> getService2(service: Service?, serviceName: String): Result<T, ErrorReport> {
        if (service != null) {
            if (service.isRunning() == true) {
                return Ok(service as T)
            } else {
                val report = ErrorReport(
                    header = IOPubServiceErrors.IOPubServiceNotRunning,
                    data = IOPubServiceErrors.IOPubServiceNotRunning.Data("${this.javaClass.canonicalName}:getService")
                )
                return Err(report)
            }
        } else {
            val report = ErrorReport(
                header = ServiceErrors.ServiceNull,
                data = ServiceErrors.ServiceNull.Data(serviceName)
            )
            return Err(report)
        }
    }

    override fun conv(): KernelContextReadOnlyConv {
        return this.convInstance
    }

    override fun zContext(): ZContext {
        return this.zcontext
    }
}
