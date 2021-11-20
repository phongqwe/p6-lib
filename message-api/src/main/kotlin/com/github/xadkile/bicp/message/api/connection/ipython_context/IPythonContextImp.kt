package com.github.xadkile.bicp.message.api.connection.ipython_context

import com.github.michaelbull.result.*
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatService
import com.github.xadkile.bicp.message.api.connection.heart_beat.LiveCountHeartBeatService
import com.github.xadkile.bicp.message.api.protocol.KernelConnectionFileContent
import com.github.xadkile.bicp.message.api.protocol.other.MsgCounterImp
import com.github.xadkile.bicp.message.api.protocol.other.MsgIdGenerator
import com.github.xadkile.bicp.message.api.protocol.other.SequentialMsgIdGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgCounter
import org.zeromq.SocketType
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
    val ipythonConfig: IPythonConfig, val zcontext: ZContext,
) : IPythonContext {

    private val launchCmd: List<String> = this.ipythonConfig.makeLaunchCmmd()
    private var process: Process? = null
    private var connectionFileContent: KernelConnectionFileContent? = null
    private var connectionFilePath: Path? = null
    private var session: Session? = null
    private var channelProvider: ChannelProvider? = null
    private var msgEncoder: MsgEncoder? = null
    private var msgIdGenerator: MsgIdGenerator? = null
    private var msgCounter: MsgCounter? = null
    private var senderProvider: SenderProvider? = null
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
    override fun startIPython(): Result<Unit, Exception> {
        if (this.isRunning()) {
            return Ok(Unit)
        } else {
            val processBuilder = ProcessBuilder(launchCmd)
            try {
                this.process = processBuilder.inheritIO().start()
                // rmd: wait for process to come live
                this.poll(50) { this.process?.isAlive != true }

                this.process?.onExit()?.thenApply {
                    destroyResource()
                    this.onAfterStopListener.run(this)
                }

                // rmd: read connection file
                this.connectionFilePath = Paths.get(ipythonConfig.connectionFilePath)
                this.poll(50) { !Files.exists(this.connectionFilePath!!) }
                this.connectionFileContent =
                    KernelConnectionFileContent.fromJsonFile(ipythonConfig.connectionFilePath).unwrap()

                // rmd: create resources, careful with the order of resource initiation, some must be initialized first
                this.channelProvider = ChannelProviderImp(this.connectionFileContent!!)
                this.session = SessionImp.autoCreate(this.connectionFileContent?.key!!)
                this.msgEncoder = MsgEncoderImp(this.connectionFileContent?.key!!)
                this.msgCounter = MsgCounterImp()
                this.msgIdGenerator = SequentialMsgIdGenerator(this.session!!.getSessionId(), this.msgCounter!!)

                // rmd: start heart beat service
                this.hbService = LiveCountHeartBeatService(
                    hbSocket = this.zcontext.createSocket(SocketType.REQ).also {
                        it.connect(this.channelProvider!!.getHeartbeatChannel().makeAddress())
                    },
                    zContext = this.zcontext
                ).also { it.start() }
                // rmd: wait until heart beat service is live
                this.poll(50) { this.hbService?.isServiceRunning() != true /*|| this.hbService?.isHBAlive() !=true*/ }

                // rmd: senderProvider depend on heart beat service, so it must be initialized after hb service is created
                this.senderProvider =
                    SenderProviderImp(this.channelProvider!!, this.zcontext, this.msgEncoder!!, this.hbService!!.conv())
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
                // reminder: destroying ipython process will invoke destroyResource()
                // that destroys other resource too.
                this.process?.destroy()
                runBlocking {
                    // polling until the process is dead completely
                    while (this@IPythonContextImp.process?.isAlive == true) {
                        delay(10)
                    }
                }
                this.onAfterStopListener.run(this)
            }
            return Ok(Unit)
        } catch (e: Exception) {
            return Err(e)
        }
    }

    private fun destroyResource() {
        val cpath = this.connectionFilePath

        if (cpath != null) {
            // delete connection file
            Files.delete(cpath)
            runBlocking {
                // wait until file is deleted completely
                while (Files.exists(cpath)) {
                    delay(50)
                }
            }
            this.connectionFilePath = null
        }
        // destroy other resources
        this.process = null
        this.connectionFileContent = null
        this.session = null
        this.channelProvider = null
        this.msgEncoder = null
        this.msgIdGenerator = null
        this.msgCounter = null
        this.senderProvider = null
        // stop hb service
        // should Context ditatate the hb service status?
        /**
         * The purpose of heart beat service is being able to watch ZMQ:
         * - detect when it is up, when it is down.
         * - detect if computation is still going on, so that waiting for computation completion is justified
         * When IPython context goes down, naturally, it will bring down ZMQ.
         * If I tie heart beat service to context, then it no longer service the first purpose.
         * However, because heart beat service depend on context info (port, address). An independent heart beat service has the potential to mis-align its info with the currrent context.
         * When a context start and stop, it will change the port to ZMQ services. Therefore, heart beat service of previous context cannot react to the newly create ZMQ services.
         * Therefore, my current heart beat service must be tied to iPython context.
         * sender depends on a heart beat service to wait for its computation to complete.
         *
         * So, if I use a single hb service. Then the context need to hold an instance of hb service to pass it to senders.
         *
         *
         *
         * If I want an independent service, I must make it a reactor that reacts to context change event.
         */
        this.hbService?.stop()
        this.hbService = null
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
        if (this.isRunning()) {
            return Ok(this.channelProvider!!)
        } else {
            return Err(ipythonIsDownErr)
        }
    }

    override fun getSenderProvider(): Result<SenderProvider, Exception> {
        return Err(IllegalStateException("not impl"))
    }

    override fun getMsgEncoder(): Result<MsgEncoder, Exception> {
        if (this.isRunning()) {
            return Ok(this.msgEncoder!!)
        } else {
            return Err(ipythonIsDownErr)
        }
    }

    override fun getMsgIdGenerator(): Result<MsgIdGenerator, Exception> {
        if (this.isRunning()) {
            return Ok(this.msgIdGenerator!!)
        } else {
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

    private fun getStatuses():List<Boolean>{
        val isProcessLive = this.process?.isAlive ?: false
        val isFileWritten =
            this.connectionFilePath != null && this.connectionFilePath?.let { Files.exists(it) } ?: false
        val connectionFileIsRead = this.connectionFileContent != null
        val isSessonOk = this.session!=null
        val isChannelProviderOk = this.channelProvider!=null
        val isMsgEncodeOk = this.msgEncoder!=null
        val isMsgCounterOk = this.msgCounter !=null
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
