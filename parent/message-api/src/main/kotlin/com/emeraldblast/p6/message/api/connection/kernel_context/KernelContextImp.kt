package com.emeraldblast.p6.message.api.connection.kernel_context

import com.github.michaelbull.result.*
import com.emeraldblast.p6.common.exception.error.CommonErrors
import com.emeraldblast.p6.common.exception.error.ErrorReport
import com.emeraldblast.p6.message.api.connection.kernel_context.context_object.*
import com.emeraldblast.p6.message.api.connection.kernel_context.context_object.ChannelProvider
import com.emeraldblast.p6.message.api.connection.kernel_context.context_object.ChannelProviderImp
import com.emeraldblast.p6.message.api.connection.kernel_context.errors.KernelErrors
import com.emeraldblast.p6.message.api.connection.service.Service
import com.emeraldblast.p6.message.api.connection.service.errors.ServiceErrors
import com.emeraldblast.p6.message.api.connection.service.heart_beat.HeartBeatService
import com.emeraldblast.p6.message.api.connection.service.heart_beat.HeartBeatServiceFactory
import com.emeraldblast.p6.message.api.connection.service.heart_beat.LiveCountHeartBeatServiceCoroutine
import com.emeraldblast.p6.message.api.connection.service.iopub.IOPubListenerService
import com.emeraldblast.p6.message.api.connection.service.iopub.IOPubListenerServiceFactory
import com.emeraldblast.p6.message.api.connection.service.iopub.IOPubListenerServiceImpl
import com.emeraldblast.p6.message.api.connection.service.iopub.errors.IOPubServiceErrors
import com.emeraldblast.p6.message.api.connection.service.zmq_services.ZMQListenerService
import com.emeraldblast.p6.message.api.connection.service.zmq_services.imp.REPService
import com.emeraldblast.p6.message.api.connection.service.zmq_services.imp.REPServiceFactory
import com.emeraldblast.p6.message.api.connection.service.zmq_services.msg.P6Response
import com.emeraldblast.p6.message.api.message.protocol.KernelConnectionFileContent
import com.emeraldblast.p6.message.api.message.protocol.other.MsgCounterImp
import com.emeraldblast.p6.message.api.message.protocol.other.MsgIdGenerator
import com.emeraldblast.p6.message.api.message.protocol.other.RandomMsgIdGenerator
import com.emeraldblast.p6.message.api.other.Sleeper
import com.emeraldblast.p6.message.di.MsgApiCommonLogger
import com.emeraldblast.p6.message.di.RepServiceLogger
import com.emeraldblast.p6.message.di.ServiceCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    private val kernelConfig: KernelConfig,
    private val zcontext: ZContext,
//    @KernelCoroutineScope
//    private val kernelCoroutineScope: CoroutineScope,
//    // x: dispatcher (a group of threads) on which network communication services run on
//    @ServiceCoroutineDispatcher
//    private val networkServiceDispatcher: CoroutineDispatcher = Dispatchers.IO,
//    @RepServiceLogger
//    private val repServiceLogger:Logger?=null,
    @MsgApiCommonLogger
    private val commonLogger:Logger?=null,
    private var msgCounter: MsgCounter,
    private var msgIdGenerator: MsgIdGenerator,
    private val channelProviderFactory:ChannelProviderFactory,
    private val msgEncoderFactory: MsgEncoderFactory,
    private val socketFactoryFactory:SocketFactoryFactory,
    private val sessionFactory: SessionFactory,
    private val senderProviderFactory: SenderProviderFactory,
    private val heartBeatServiceFactory: HeartBeatServiceFactory,
    private val ioPubListenerServiceFactory: IOPubListenerServiceFactory,
    private val repServiceFactory: REPServiceFactory,
) : KernelContext {

    private val kernelTimeOut = kernelConfig.timeOut

    // x: Context-related objects
    private var process: Process? = null
    private var connectionFileContent: KernelConnectionFileContent? = null
    private var connectionFilePath: Path? = null
    private var session: Session? = null

    private var channelProvider: ChannelProvider? = null
    private var msgEncoder: MsgEncoder? = null


    private var senderProvider: SenderProvider? = null
    private var socketFactory: SocketFactory? = null

    // x: Context-related services
    private var hbService: HeartBeatService? = null
    private var ioPubService: IOPubListenerService? = null
    private var zmqREPService:ZMQListenerService<P6Response>? = null

    // x: events listeners
    private var onBeforeStopListener: OnKernelContextEvent = OnKernelContextEvent.Nothing
    private var onAfterStopListener: OnKernelContextEvent = OnKernelContextEvent.Nothing
    private var onKernelStartedListener: OnKernelContextEvent = OnKernelContextEvent.Nothing

    companion object {
        private val kernelDownReport = ErrorReport(
            header = KernelErrors.KernelDown.header,
            data = KernelErrors.KernelDown.Data(""),
        )
    }

    override fun getZmqREPService(): Result<ZMQListenerService<P6Response>,ErrorReport> {
        val sv = getService<ZMQListenerService<P6Response>>(this.zmqREPService, "ZMQ REP listener service")
        return sv
    }

    override suspend fun startAll(): Result<Unit, ErrorReport> {
        val kernelRS: Result<Unit, ErrorReport> = this.startKernel()
        if (kernelRS is Ok) {
            val serviceRs: Result<Unit, ErrorReport> = this.startServices()
            return serviceRs
        } else {
            return kernelRS
        }
    }

    override suspend fun startKernel(): Result<Unit, ErrorReport> {
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
            val waitConnectionFileWritten: Result<Unit, ErrorReport> =
                Sleeper.delayUntil(50,
                    kernelTimeOut.connectionFileWriteTimeout) { Files.exists(this.connectionFilePath!!) }

            if (waitConnectionFileWritten is Err) {
                val report = ErrorReport(KernelErrors.CantWriteConnectionFile.header,
                    KernelErrors.CantWriteConnectionFile.Data(this.connectionFilePath),
                    )
                return Err(report)
            }

            this.connectionFileContent = this.kernelConfig.kernelConnectionFileContent!!
            val connectionFiles = this.connectionFileContent!!

            // x: create resources, careful with the order of resource initiation,
            // x: some must be initialized first
            // x: must NOT use getters here because getters always check for kernel status before return derivative objects
            this.channelProvider = channelProviderFactory.create(connectionFiles)
            this.socketFactory = socketFactoryFactory.create(this.channelProvider!!, this.zcontext)
            this.session = sessionFactory.create(connectionFiles.key)
            this.msgEncoder = msgEncoderFactory.create(connectionFiles.key)
            this.senderProvider = senderProviderFactory.create(this)
            this.onKernelStartedListener.run(this)
            return Ok(Unit)
        }
    }


    private suspend fun startKernelProcess(): Result<Process, ErrorReport> {
        val processBuilder = ProcessBuilder(this.kernelConfig.makeCompleteLaunchCmmd())
        try {
            val p: Process = processBuilder.inheritIO().start()
            val waitRs = Sleeper.delayUntil(50, kernelTimeOut.processInitTimeOut) { p.isAlive }
            if (waitRs is Err) {
                val report = ErrorReport(
                    KernelErrors.CantStartProcess.header,
                    KernelErrors.CantStartProcess.Data(this.kernelConfig.makeCompleteLaunchCmmd().joinToString(" ")))
                return Err(report)
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



    override suspend fun startServices(): Result<Unit, ErrorReport> {
        if (this.isKernelRunning()) {

            val hbSv = heartBeatServiceFactory.create(
                kernelContext = this,
                liveCount = 20,
                pollTimeOut = 1000,
                startTimeOut = this.kernelConfig.timeOut.serviceInitTimeOut
            )
            this.hbService = hbSv

            val hbStartRs:Result<Unit, ErrorReport> = hbSv.start()
            if (hbStartRs is Err) {
                this.hbService?.stop()
                this.hbService = null
                return hbStartRs
            }

            val ioPubSv = ioPubListenerServiceFactory.create(
                kernelContext = this,
                defaultHandler = {},
                parseExceptionHandler = {},
                startTimeOut = this.kernelConfig.timeOut.serviceInitTimeOut
            )
            this.ioPubService=ioPubSv

            val ioPubStartRs:Result<Unit, ErrorReport> = ioPubSv.start()
            if (ioPubStartRs is Err) {
                this.ioPubService?.stop()
                this.ioPubService = null
                return ioPubStartRs
            }

            val zmqREPService = repServiceFactory.create(
                kernelContext = this,
            )

            this.zmqREPService = zmqREPService
            val zmqListenerServiceStartRs = zmqREPService.start()
            if(zmqListenerServiceStartRs is Err){
                this.zmqREPService?.stop()
                this.zmqREPService = null
                return zmqListenerServiceStartRs
            }

            return Ok(Unit)

        } else {
            val report = ErrorReport(
                header = KernelErrors.KernelDown.header,
                data = KernelErrors.KernelDown.Data(this::class.java.canonicalName)
            )
            return Err(report)
        }
    }


    override suspend fun stopAll(): Result<Unit, ErrorReport> {
        val r: Result<Unit, ErrorReport> = stopServices().andThen {
            stopKernel()
        }
        return r
    }

    private suspend fun stopKernelProcess2(): Result<Unit, ErrorReport> {
        if (this.process != null) {
            this.process?.destroyForcibly()
            // x: polling until the process is completely dead
            val stopRs: Result<Unit, ErrorReport> =
                Sleeper.delayUntil(50, kernelTimeOut.processStopTimeout) { this.process?.isAlive == false }
            val rs = stopRs.mapError {
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
            return Err(kernelDownReport)
        }
    }


    override suspend fun stopServices(): Result<Unit, ErrorReport> {

        val errorList = mutableListOf<ErrorReport>()
        val ioPubStopRs = this.ioPubService?.stop() ?: Ok(Unit)
        if (ioPubStopRs is Err) {
            errorList.add(ioPubStopRs.error)
        }else{
            this.ioPubService = null
        }

        val hbStopRs = this.hbService?.stop() ?: Ok(Unit)
        if (hbStopRs is Err) {
            errorList.add(hbStopRs.error)
        }else{
            this.hbService = null
        }

        val zmqRepStopRs = this.zmqREPService?.stop() ?: Ok(Unit)
        if(zmqRepStopRs is Err){
            errorList.add(zmqRepStopRs.error)
        }else{
            this.zmqREPService = null
        }

        if(errorList.isNotEmpty()){
            return Err(ErrorReport(
                header = CommonErrors.MultipleErrors.header,
                data = CommonErrors.MultipleErrors.Data(errorList)
            ))
        }else{
            return Ok(Unit)
        }
    }

    override suspend fun stopKernel(): Result<Unit, ErrorReport> {
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
        if (this.process!=null && this.process?.isAlive == true) {
            return Ok(this.process!!)
        } else {
            val report = ErrorReport(
                header = KernelErrors.KernelDown.header,
                data = KernelErrors.KernelDown.Data("getKernelProcess")
            )
            return Err(report)
        }
    }

    override fun getKernelInputStream(): Result<InputStream, ErrorReport> {
        return this.getKernelProcess().map { it.inputStream }
    }

    override fun getKernelOutputStream(): Result<OutputStream, ErrorReport> {
        return this.getKernelProcess().map { it.outputStream }
    }

    override suspend fun restartKernel(): Result<Unit, ErrorReport> {
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
            return Err(kernelDownReport)
        }
    }


    override fun getSession(): Result<Session, ErrorReport> {
        if (this.isKernelRunning()) {
            return Ok(this.session!!)
        } else {
            return Err(kernelDownReport)
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
        val isSenderProviderOk = this.senderProvider != null

        val rt = listOf(
            isProcessLive, isFileWritten, connectionFileIsRead,
            isSessonOk, isChannelProviderOk, isMsgEncodeOk, isSenderProviderOk,
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


    override fun getIOPubListenerService(): Result<IOPubListenerService, ErrorReport> {
        val sv = getService<IOPubListenerService>(this.ioPubService, "IO Pub service")
        return sv
    }


    override fun getHeartBeatService(): Result<HeartBeatService, ErrorReport> {
        val sv = getService<HeartBeatService>(this.hbService, "heart beat service")
        return sv
    }

    private fun <T> getService(service: Service?, serviceName: String): Result<T, ErrorReport> {
        if (service != null) {
            if (service.isRunning()) {
                return Ok(service as T)
            } else {
                val report = ErrorReport(
                    header = IOPubServiceErrors.IOPubServiceNotRunning.header,
                    data = IOPubServiceErrors.IOPubServiceNotRunning.Data("${this.javaClass.canonicalName}:getService")
                )
                return Err(report)
            }
        } else {
            val report = ErrorReport(
                header = ServiceErrors.ServiceNull.header,
                data = ServiceErrors.ServiceNull.Data(serviceName)
            )
            return Err(report)
        }
    }

    override fun zContext(): ZContext {
        return this.zcontext
    }
}
