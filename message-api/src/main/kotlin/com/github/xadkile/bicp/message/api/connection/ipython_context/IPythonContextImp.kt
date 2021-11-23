package com.github.xadkile.bicp.message.api.connection.ipython_context

import com.github.michaelbull.result.*
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatService
import com.github.xadkile.bicp.message.api.connection.heart_beat.coroutine.LiveCountHeartBeatServiceCoroutine
//import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatServiceUpdater
import com.github.xadkile.bicp.message.api.other.Sleeper
import com.github.xadkile.bicp.message.api.protocol.KernelConnectionFileContent
import com.github.xadkile.bicp.message.api.protocol.other.MsgCounterImp
import com.github.xadkile.bicp.message.api.protocol.other.MsgIdGenerator
import com.github.xadkile.bicp.message.api.protocol.other.RandomMsgIdGenerator
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
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
class IPythonContextImp @Inject internal constructor(
    val ipythonConfig: KernelConfig, val zcontext: ZContext,
) : IPythonContext {

    private val launchCmd: List<String> = this.ipythonConfig.makeCompleteLaunchCmmd()
    private var process: Process? = null
    private var connectionFileContent: KernelConnectionFileContent? = null
    private var connectionFilePath: Path? = null
    private var session: Session? = null
    private var channelProvider: ChannelProvider? = null
    private var msgEncoder: MsgEncoder? = null
    private var msgIdGenerator: MsgIdGenerator? = null
    private var msgCounter: MsgCounter? = null
    private var senderProvider: SenderProvider? = null
    private var socketProvider:SocketProvider? = null
    private var hbService: HeartBeatService? = null

    private var onBeforeStopListener: OnIPythonContextEvent = OnIPythonContextEvent.Nothing
    private var onAfterStopListener: OnIPythonContextEvent = OnIPythonContextEvent.Nothing
    private var onProcessStartListener: OnIPythonContextEvent = OnIPythonContextEvent.Nothing

    private val convenientInterface = IPythonContextReadOnlyConvImp(this)

    companion object {
        private val ipythonIsDownErr = IPythonIsDownException("IPython process is not running")
    }

    private fun poll(sleepTime: Long, falseCondition: () -> Boolean) {
        while (falseCondition()) {
            Thread.sleep(sleepTime)
        }
    }

    /**
     * This method returns when:
     * - ipython process is up
     * - connection file is written to disk
     * - heart beat service is running + zmq is live (heart beat is ok)
     */
    @OptIn(DelicateCoroutinesApi::class)
    override fun startIPython(): Result<Unit, Exception> {
        if (this.isRunning()) {
            return Ok(Unit)
        } else {
            val processBuilder = ProcessBuilder(launchCmd)
            try {
                this.process = processBuilder.inheritIO().start()
                // rmd: wait for process to come live
                Sleeper.sleepUntil(50) { this.process?.isAlive == true }

                // rmd: read connection file
                this.connectionFilePath = Paths.get(ipythonConfig.getConnectionFilePath())
                Sleeper.sleepUntil(50){Files.exists(this.connectionFilePath!!)}

                this.connectionFileContent =
                    KernelConnectionFileContent.fromJsonFile(ipythonConfig.getConnectionFilePath()).unwrap()

                // rmd: create resources, careful with the order of resource initiation,
                // some must be initialized first
                this.channelProvider = ChannelProviderImp(this.connectionFileContent!!)
                this.socketProvider = SocketProviderImp(this.channelProvider!!,this.zcontext)
                this.session = SessionImp.autoCreate(this.connectionFileContent?.key!!)
                this.msgEncoder = MsgEncoderImp(this.connectionFileContent?.key!!)
                this.msgCounter = MsgCounterImp()
//                this.msgIdGenerator = SequentialMsgIdGenerator(this.session!!.getSessionId(), this.msgCounter!!)
                this.msgIdGenerator = RandomMsgIdGenerator()

                // rmd: start heart beat service
                this.hbService = LiveCountHeartBeatServiceCoroutine(
                    socketProvider = this.socketProvider!!,
                    zContext = this.zcontext,
                    cScope = GlobalScope,
                ).also { it.start() }

                // rmd: wait until heart beat service is live
                Sleeper.sleepUntil(50){this.hbService?.isServiceRunning() == true}
                Sleeper.sleepUntil(50){this.hbService?.isHBAlive() == true}

                // x: senderProvider depend on heart beat service,
                // x: so it must be initialized after hb service is created
                this.senderProvider =
                    SenderProviderImp( this.zcontext, this.msgEncoder!!, this.hbService!!.conv(), this.socketProvider!!)
                this.onProcessStartListener.run(this)
                return Ok(Unit)
            } catch (e: Exception) {
                return Err(e)
            }
        }
    }
    override fun stopIPython(): Result<Unit, Exception> {
        if (this.isNotRunning()) {
            return Ok(Unit)
        }
        try {
            if (this.process != null) {
                this.onBeforeStopListener.run(this)
                this.process?.destroy()
                // rmd: polling until the process is completely dead
                Sleeper.sleepUntil(50){this.process?.isAlive == false}
                this.process = null
                this.onAfterStopListener.run(this)
            }
            destroyResource()
            this.onAfterStopListener.run(this)
            return Ok(Unit)
        } catch (e: Exception) {
            return Err(e)
        }
    }

    override fun getSocketProvider(): Result<SocketProvider, Exception> {
        if(this.isRunning()){
            return Ok(this.socketProvider!!)
        }else{
            return Err(ipythonIsDownErr)
        }
    }

    private fun destroyResource() {
        val cpath = this.connectionFilePath

        if (cpath != null) {
            // x: delete connection file
            Files.delete(cpath)
            // rmd: wait until file is deleted completely
            Sleeper.sleepUntil(50){ !Files.exists(cpath) }
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
        this.hbService?.stop()
        this.hbService = null
        this.socketProvider = null
    }

    override fun getIPythonProcess(): Result<Process, Exception> {
        if (this.isRunning()) {
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

    override fun restartIPython(): Result<Unit, Exception> {
        if (this.isRunning()) {
            val rt = this.stopIPython()
                .andThen {
                    this.startIPython()
                }
            return rt
        } else {
            return Err(IPythonContextIllegalStateException("IPythonProcessManager is stopped, thus cannot be restarted"))
        }
    }

    override fun getConnectionFileContent(): Result<KernelConnectionFileContent, Exception> {
        if (this.isRunning()) {
            return Ok(this.connectionFileContent!!)
        } else {
            return Err(ipythonIsDownErr)
        }
    }

    override fun getSession(): Result<Session, Exception> {
        if (this.isRunning()) {
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

    private fun<T> checkRunningAndGet(that:()->T):Result<T,Exception>{
        if(this.isRunning()){
            return Ok(that())
        }else{
            return Err(ipythonIsDownErr)
        }
    }

    override fun isRunning(): Boolean {
        val rt = this.getStatuses().all { it }
        return rt
    }

    override fun isNotRunning(): Boolean {
        val rt = this.getStatuses().all { !it }
        return rt
    }

    private fun getStatuses(): List<Boolean> {
        val isProcessLive = this.process?.isAlive ?: false
        val isFileWritten = this.connectionFilePath?.let { Files.exists(it) } ?: false
        val connectionFileIsRead = this.connectionFileContent != null
        val isSessonOk = this.session != null
        val isChannelProviderOk = this.channelProvider != null
        val isMsgEncodeOk = this.msgEncoder != null
        val isMsgCounterOk = this.msgCounter != null
        val isSenderProviderOk = this.senderProvider != null
        val isHBServiceRunning = this.hbService?.isServiceRunning() ?: false

        val rt = listOf(
            isProcessLive, isFileWritten, isHBServiceRunning, connectionFileIsRead,
            isSessonOk, isChannelProviderOk, isMsgEncodeOk, isMsgCounterOk, isSenderProviderOk
        )
        return rt
    }

    override fun setOnBeforeProcessStopListener(listener: OnIPythonContextEvent) {
        this.onBeforeStopListener = listener
    }

    override fun removeBeforeOnProcessStopListener() {
        this.onBeforeStopListener = OnIPythonContextEvent.Nothing
    }

    override fun setOnAfterProcessStopListener(listener: OnIPythonContextEvent) {
        this.onAfterStopListener = listener
    }

    override fun removeAfterOnProcessStopListener() {
        this.onAfterStopListener = OnIPythonContextEvent.Nothing
    }

    override fun setOnStartProcessListener(listener: OnIPythonContextEvent) {
        this.onProcessStartListener = listener
    }

    override fun removeOnProcessStartListener() {
        this.onProcessStartListener = OnIPythonContextEvent.Nothing
    }

    override fun getHeartBeatService(): Result<HeartBeatService, Exception> {
        if (this.isRunning()) {
            return Ok(this.hbService!!)
        } else {
            return Err(ipythonIsDownErr)
        }
    }

    override fun conv(): IPythonContextReadOnlyConv {
        return this.convenientInterface
    }

    override fun zContext(): ZContext {
        return this.zcontext
    }
}
