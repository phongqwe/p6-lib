package com.github.xadkile.bicp.message.api.connection

import com.github.michaelbull.result.*
import com.github.xadkile.bicp.message.api.protocol.KernelConnectionFileContent
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Inject

class IPythonContextImp @Inject constructor(private val ipythonConfig: IPythonConfig) : IPythonContext {

    private val launchCmd: List<String> by lazy {
        this.ipythonConfig.makeLaunchCmmd()
    }

    private var process: Process? = null
    private var _connectionFileContent: KernelConnectionFileContent? = null
    private var connectionFilePath: Path? = null
    private var _session: Session? = null
    private var _channelProvider: ChannelProvider? = null
    private var _msgEncoder:MsgEncoder? = null

    companion object {
        private val ipythonNotRunningMsg = "IPython process is not running"
    }

    override fun startIPython(): Result<Unit, Exception> {
        if (this.isRunning()) {
            return Ok(Unit)
        } else {
            val processBuilder = ProcessBuilder(launchCmd)
            try {
                this.process = processBuilder.inheritIO().start()
                Thread.sleep(this.ipythonConfig.milliSecStartTime)
                val cf: Result<KernelConnectionFileContent, IOException> =
                    KernelConnectionFileContent.fromJsonFile(ipythonConfig.connectionFilePath)
                val rt: Result<Unit, Exception> = cf.map {
                    this._connectionFileContent = it
                }
                this.connectionFilePath = Paths.get(ipythonConfig.connectionFilePath)
                this._channelProvider = ChannelProviderImp(this._connectionFileContent!!)
                this._session = SessionImp.autoCreate(this._connectionFileContent?.key!!)
                this._msgEncoder = MsgEncoderImp(this._connectionFileContent?.key!!)
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
                this.process?.onExit()?.thenApply {
                    // delete connection file
                    val cpath = this.connectionFilePath
                    if (cpath != null) {
                        Files.delete(cpath)
                        runBlocking {
                            // wait until file is deleted completely
                            while (Files.exists(cpath)) {
                                delay(50)
                            }
                        }

                    }
                }
                this.process?.destroy()
                runBlocking {
                    // wait until the process is dead completely
                    while (this@IPythonContextImp.process?.isAlive == true) {
                        delay(50)
                    }
                }
                this.process = null
                this.connectionFilePath = null
                this._connectionFileContent = null
                this._session = null
                this._channelProvider = null
                this._msgEncoder=null
            }
            return Ok(Unit)
        } catch (e: Exception) {
            return Err(e)
        }
    }

    override fun getIPythonProcess(): Result<Process,Exception> {
        if(this.isRunning()){
            return Ok(this.process!!)
        }else{
            return Err(FaultyConnectionException(ipythonNotRunningMsg))
        }
    }

    override fun restartIPython(): Result<Unit, Exception> {
        if (this.isRunning()) {
            val rt = this.stopIPython()
                .andThen {
                    this.startIPython()
                }
            return rt
        } else {
            return Err(IllegalStateException("IPythonProcessManager is stopped, thus cannot be restarted"))
        }
    }

    override fun getConnectionFileContent(): Result<KernelConnectionFileContent,Exception> {
        if(this.isRunning()){
            return Ok(this._connectionFileContent!!)
        }else{
            return Err(FaultyConnectionException(ipythonNotRunningMsg))
        }

    }

    override fun getSession(): Result<Session,Exception> {
        if (this.isRunning()) {
            return Ok(this._session!!)
        } else {
            return Err(FaultyConnectionException(ipythonNotRunningMsg))
        }
    }

    override fun getChannelProvider(): Result<ChannelProvider,Exception> {
        if (this.isRunning()) {
            return Ok(this._channelProvider!!)
        } else {
            return Err(FaultyConnectionException(ipythonNotRunningMsg))
        }
    }

    override fun getSenderProvider(): SenderProvider {
        TODO("Not yet implemented")
    }

    override fun getMsgEncoder(): Result<MsgEncoder, Exception> {
        if(this.isRunning()){
            return Ok(this._msgEncoder!!)
        }else{
            return Err(FaultyConnectionException(ipythonNotRunningMsg))
        }
    }

    fun isRunning(): Boolean {
        return (this.process?.isAlive
            ?: false) && this.connectionFilePath != null && this._connectionFileContent != null
    }

    fun isNotRunning(): Boolean {
        return !this.isRunning()
    }

}
