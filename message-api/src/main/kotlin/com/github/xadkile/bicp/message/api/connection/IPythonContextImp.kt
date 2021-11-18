package com.github.xadkile.bicp.message.api.connection

import com.github.michaelbull.result.*
import com.github.xadkile.bicp.message.api.connection.process_watcher.OnStopWatcher
import com.github.xadkile.bicp.message.api.protocol.KernelConnectionFileContent
import com.github.xadkile.bicp.message.api.protocol.other.MsgCounterImp
import com.github.xadkile.bicp.message.api.protocol.other.MsgIdGenerator
import com.github.xadkile.bicp.message.api.protocol.other.SequentialMsgIdGenerator
import com.github.xadkile.bicp.message.api.exception.FaultyIPythonConnectionException
import com.github.xadkile.bicp.message.api.exception.IPythonContextIllegalStateException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgCounter
import org.zeromq.ZContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IPythonContextImp @Inject internal constructor(
     var ipythonConfig: IPythonConfig, var zcontext:ZContext) : IPythonContext {

    private val launchCmd: List<String> = this.ipythonConfig.makeLaunchCmmd()
    private var process: Process? = null
    private var connectionFileContent: KernelConnectionFileContent? = null
    private var connectionFilePath: Path? = null
    private var session: Session? = null
    private var channelProvider: ChannelProvider? = null
    private var msgEncoder:MsgEncoder? = null
    private var msgIdGenerator:MsgIdGenerator? = null
    private var msgCounter: MsgCounter? = null
    private var senderProvider:SenderProvider?=null

    private var onBeforeStopListener:OnIPythonContextEvent = OnIPythonContextEvent.Nothing
    private var onAfterStopListener:OnIPythonContextEvent = OnIPythonContextEvent.Nothing
    private var onProcessStartListener:OnIPythonContextEvent = OnIPythonContextEvent.Nothing

    companion object {
        private val faultyConnection = FaultyIPythonConnectionException("IPython process is not running")
    }

    override fun startIPython(): Result<Unit, Exception> {
        if (this.isRunning()) {
            return Ok(Unit)
        } else {
            val processBuilder = ProcessBuilder(launchCmd)
            try {
                this.process = processBuilder.inheritIO().start()
                Thread.sleep(this.ipythonConfig.milliSecStartTime)
                this.onProcessStartListener.run(this)
                this.process?.onExit()?.thenApply {
                    destroyResource()
                    this.onAfterStopListener.run(this)
                }
                val cf: Result<KernelConnectionFileContent, IOException> =
                    KernelConnectionFileContent.fromJsonFile(ipythonConfig.connectionFilePath)
                val rt: Result<Unit, Exception> = cf.map {
                    this.connectionFileContent = it
                }
                this.connectionFilePath = Paths.get(ipythonConfig.connectionFilePath)
                this.channelProvider = ChannelProviderImp(this.connectionFileContent!!)
                this.session = SessionImp.autoCreate(this.connectionFileContent?.key!!)
                this.msgEncoder = MsgEncoderImp(this.connectionFileContent?.key!!)
                this.msgCounter = MsgCounterImp()
                this.msgIdGenerator = SequentialMsgIdGenerator(this.session!!.getSessionId(), this.msgCounter!!)
                this.senderProvider = SenderProviderImp(this.channelProvider!!,this.zcontext,this.msgEncoder!!)
                return rt
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
                // destroying ipython process will destroy other resource too
                this.onBeforeStopListener.run(this)
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

    private fun destroyResource(){
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
        this.process = null
        this.connectionFileContent = null
        this.session = null
        this.channelProvider = null
        this.msgEncoder=null
        this.msgIdGenerator = null
        this.msgCounter = null
        this.senderProvider = null
    }

    override fun getIPythonProcess(): Result<Process,Exception> {
        if(this.isRunning()){
            return Ok(this.process!!)
        }else{
            return Err(faultyConnection)
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

    override fun getConnectionFileContent(): Result<KernelConnectionFileContent,Exception> {
        if(this.isRunning()){
            return Ok(this.connectionFileContent!!)
        }else{
            return Err(faultyConnection)
        }
    }

    override fun getSession(): Result<Session,Exception> {
        if (this.isRunning()) {
            return Ok(this.session!!)
        } else {
            return Err(faultyConnection)
        }
    }

    override fun getChannelProvider(): Result<ChannelProvider,Exception> {
        if (this.isRunning()) {
            return Ok(this.channelProvider!!)
        } else {
            return Err(faultyConnection)
        }
    }

    override fun getSenderProvider(): Result<SenderProvider, Exception> {
        return Err(IllegalStateException("zz"))
    }

    override fun getMsgEncoder(): Result<MsgEncoder, Exception> {
        if(this.isRunning()){
            return Ok(this.msgEncoder!!)
        }else{
            return Err(faultyConnection)
        }
    }

    override fun getMsgIdGenerator(): Result<MsgIdGenerator, Exception> {
        if(this.isRunning()){
            return Ok(this.msgIdGenerator!!)
        }else{
           return Err(faultyConnection)
        }
    }

    fun isRunning(): Boolean {
        return (this.process?.isAlive
            ?: false) && this.connectionFilePath != null && this.connectionFileContent != null
    }

    fun isNotRunning(): Boolean {
        return !this.isRunning()
    }

    override fun setOnBeforeProcessStopListener(listener: OnIPythonContextEvent) {
        this.onBeforeStopListener  = listener
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
}
