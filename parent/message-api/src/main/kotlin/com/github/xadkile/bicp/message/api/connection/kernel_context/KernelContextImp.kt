package com.github.xadkile.bicp.message.api.connection.kernel_context

import com.github.michaelbull.result.*
import com.github.xadkile.bicp.message.api.connection.kernel_context.context_object.*
import com.github.xadkile.bicp.message.api.connection.kernel_context.exception.KernelContextIllegalStateException
import com.github.xadkile.bicp.message.api.connection.kernel_context.exception.KernelIsDownException
import com.github.xadkile.bicp.message.api.connection.service.heart_beat.HeartBeatService
import com.github.xadkile.bicp.message.api.connection.service.heart_beat.coroutine.LiveCountHeartBeatServiceCoroutine
import com.github.xadkile.bicp.message.api.connection.service.iopub.IOPubListenerService
import com.github.xadkile.bicp.message.api.connection.service.iopub.IOPubListenerServiceImpl
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
 *  [ipythonConfig] is fixed, read from an external file and only change after application start.
 */
@Singleton
class KernelContextImp @Inject internal constructor(
    private val ipythonConfig: KernelConfig,
    private val zcontext: ZContext,
    @ApplicationCScope
    private val appCScope: CoroutineScope,
    private val networkServiceDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : KernelContext {

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
    private var onBeforeStopListener: OnIPythonContextEvent = OnIPythonContextEvent.Nothing
    private var onAfterStopListener: OnIPythonContextEvent = OnIPythonContextEvent.Nothing
    private var onKernelStartedListener: OnIPythonContextEvent = OnIPythonContextEvent.Nothing

    private val convenientInterface = KernelContextReadOnlyConvImp(this)

    companion object {
        private val ipythonIsDownErr = KernelIsDownException("IPython process is not running")
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun startKernel(): Result<Unit, Exception> {
        if (this.isKernelRunning()) {
            return Ok(Unit)
        } else {
            val processBuilder = ProcessBuilder(this.ipythonConfig.makeCompleteLaunchCmmd())
            try {
                this.process = processBuilder.inheritIO().start()

                // rmd: wait for process to come live
                Sleeper.threadSleepUntil(50) { this.process?.isAlive == true }

                // rmd: read connection file
                this.connectionFilePath = Paths.get(ipythonConfig.getConnectionFilePath())
                Sleeper.threadSleepUntil(50) { Files.exists(this.connectionFilePath!!) }
                this.connectionFileContent =
                    KernelConnectionFileContent.fromJsonFile(
                        ipythonConfig.getConnectionFilePath()).unwrap()

                // rmd: create resources, careful with the order of resource initiation,
                // some must be initialized first
                this.channelProvider = ChannelProviderImp(this.connectionFileContent!!)
                this.socketProvider = SocketProviderImp(this.channelProvider!!, this.zcontext)
                this.session = SessionImp.autoCreate(this.connectionFileContent?.key!!)
                this.msgEncoder = MsgEncoderImp(this.connectionFileContent?.key!!)
                this.msgCounter = MsgCounterImp()
                this.msgIdGenerator = RandomMsgIdGenerator()
                this.senderProvider = SenderProviderImp(this.conv())

                // rmd: start heart beat service
                this.hbService = LiveCountHeartBeatServiceCoroutine(
                    socketProvider = this.socketProvider!!,
                    zContext = this.zcontext,
                    cScope = appCScope,
                    cDispatcher = this.networkServiceDispatcher
                )

                this.ioPubService = IOPubListenerServiceImpl(
                    kernelContext = this,
                    externalScope = appCScope,
                    dispatcher = this.networkServiceDispatcher
                )

                // ph: start services
                this.startServices()

                this.onKernelStartedListener.run(this)
                return Ok(Unit)
            } catch (e: Exception) {
                return Err(e)
            }
        }
    }

    private fun startServices(){
        this.hbService!!.start()
        this.ioPubService!!.start()

        // rmd: wait until heart beat service is live
        Sleeper.threadSleepUntil(50) { this.hbService?.isServiceRunning() == true }
        Sleeper.threadSleepUntil(50) { this.hbService?.isHBAlive() == true }
    }

    override suspend fun stopKernel(): Result<Unit, Exception> {
        if (this.isKernelNotRunning()) {
            return Ok(Unit)
        }
        try {
            if (this.process != null) {
                this.onBeforeStopListener.run(this)
                this.process?.destroy()
                // rmd: polling until the process is completely dead
                Sleeper.threadSleepUntil(50) { this.process?.isAlive == false }
                this.process = null
                this.onAfterStopListener.run(this)
            }
            stopServices()
            destroyResource()
            this.onAfterStopListener.run(this)
            return Ok(Unit)
        } catch (e: Exception) {
            return Err(e)
        }
    }

    override fun getSocketProvider(): Result<SocketProvider, Exception> {
        if (this.isKernelRunning()) {
            return Ok(this.socketProvider!!)
        } else {
            return Err(ipythonIsDownErr)
        }
    }

    private suspend fun stopServices(){
        this.hbService?.stop()
        this.hbService = null
        this.ioPubService?.stop()
        this.ioPubService=null
    }

    private fun destroyResource() {
        val cpath = this.connectionFilePath

        if (cpath != null) {
            // x: delete connection file
            Files.delete(cpath)
            // rmd: wait until file is deleted completely
            Sleeper.threadSleepUntil(50) { !Files.exists(cpath) }
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
        // x: stop hb service
//        this.hbService?.stop()
//        this.hbService = null
        this.socketProvider = null

        // x: stop iopub service
//        this.ioPubService?.stop()
//        this.ioPubService=null
    }

    override fun getIPythonProcess(): Result<Process, Exception> {
        if (this.isKernelRunning()) {
            return Ok(this.process!!)
        } else {
            return Err(ipythonIsDownErr)
        }
    }

    override fun getIPythonInputStream(): Result<InputStream, Exception> {
        return this.getIPythonProcess().map { it.inputStream }
    }

    override fun getIPythonOutputStream(): Result<OutputStream, Exception> {
        return this.getIPythonProcess().map { it.outputStream }
    }

    override suspend fun restartKernel(): Result<Unit, Exception> {
        if (this.isKernelRunning()) {
            val rt = this.stopKernel()
                .andThen {
                    this.startKernel()
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
        return this.checkRunningAndGet { this.channelProvider!! }
    }

    override fun getSenderProvider(): Result<SenderProvider, Exception> {
        return this.checkRunningAndGet { this.senderProvider!! }
    }

    override fun getMsgEncoder(): Result<MsgEncoder, Exception> {
        return this.checkRunningAndGet { this.msgEncoder!! }
    }

    override fun getMsgIdGenerator(): Result<MsgIdGenerator, Exception> {
        return this.checkRunningAndGet { this.msgIdGenerator!! }
    }

    private fun <T> checkRunningAndGet(that: () -> T): Result<T, Exception> {
        if (this.isKernelRunning()) {
            return Ok(that())
        } else {
            return Err(ipythonIsDownErr)
        }
    }

    override fun isKernelRunning(): Boolean {
        val rt = this.getCoreStatus().all { it }
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
        val rt = this.getCoreStatus().all { !it }
        return rt
    }

    private fun getCoreStatus(): List<Boolean> {
        val isProcessLive = this.process?.isAlive ?: false
        val isFileWritten = this.connectionFilePath?.let { Files.exists(it) } ?: false
        val connectionFileIsRead = this.connectionFileContent != null
        val isSessonOk = this.session != null
        val isChannelProviderOk = this.channelProvider != null
        val isMsgEncodeOk = this.msgEncoder != null
        val isMsgCounterOk = this.msgCounter != null
        val isSenderProviderOk = this.senderProvider != null
//        val isHBServiceRunning = this.hbService?.isServiceRunning() ?: false

        val rt = listOf(
            isProcessLive, isFileWritten, connectionFileIsRead,
            isSessonOk, isChannelProviderOk, isMsgEncodeOk, isMsgCounterOk, isSenderProviderOk, /*isHBServiceRunning*/
        )
        return rt
    }

    override fun setOnBeforeStopListener(listener: OnIPythonContextEvent) {
        this.onBeforeStopListener = listener
    }

    override fun removeBeforeStopListener() {
        this.onBeforeStopListener = OnIPythonContextEvent.Nothing
    }

    override fun setOnAfterStopListener(listener: OnIPythonContextEvent) {
        this.onAfterStopListener = listener
    }

    override fun removeAfterStopListener() {
        this.onAfterStopListener = OnIPythonContextEvent.Nothing
    }

    override fun setKernelStartedListener(listener: OnIPythonContextEvent) {
        this.onKernelStartedListener = listener
    }

    override fun removeOnProcessStartListener() {
        this.onKernelStartedListener = OnIPythonContextEvent.Nothing
    }

    override fun getIOPubListenerService(): Result<IOPubListenerService,Exception> {
        return this.checkRunningAndGet { this.ioPubService!! }
    }

    override fun getHeartBeatService(): Result<HeartBeatService, Exception> {
        if (this.isKernelRunning()) {
            return Ok(this.hbService!!)
        } else {
            return Err(ipythonIsDownErr)
        }
    }

    override fun conv(): KernelContextReadOnlyConv {
        return this.convenientInterface
    }

    override fun zContext(): ZContext {
        return this.zcontext
    }
}
