package com.github.xadkile.bicp.message.api.connection

import com.github.michaelbull.result.*
import com.github.xadkile.bicp.message.api.protocol.KernelConnectionFileContent
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject

// TODO test this
class IPythonProcessManagerImp @Inject constructor(private val configFile: com.github.xadkile.bicp.message.api.connection.IPythonConfig) : IPythonProcessManager {

    private val launchCmd: String by lazy {
        this.configFile.makeLaunchCmmd()
    }

    private var process: Process? = null
    private var _connectionFileContent: KernelConnectionFileContent? = null
    private var connectionFilePath: Path? = null

    override fun startIPython(): Result<Unit, Exception> {
        val processBuilder = ProcessBuilder(launchCmd)
        try {
            this.process = processBuilder.inheritIO().start()
            Thread.sleep(2000)
            val cf: Result<KernelConnectionFileContent, IOException> =
                KernelConnectionFileContent.fromJsonFile(configFile.connectionFilePath)
            Thread.sleep(1000)
            val rt: Result<Unit, Exception> = cf.map {
                this._connectionFileContent = it
            }
            return rt
        } catch (e: Exception) {
            return Err(e)
        }
    }

    override fun stopIPython(): Result<Unit, Exception> {
        try {
            // clear objects relate to connection
            if (this.process != null) {
                this.process?.destroy()
                this.process = null
            }
            this._connectionFileContent = null

            // delete connection file
            val cpath = this.connectionFilePath
            if(cpath!=null){
                Files.delete(cpath)
                this.connectionFilePath = null
            }
            return Ok(Unit)
        } catch (e: Exception) {
            return Err(e)
        }
    }

    override fun getIPythonProcess(): Process? {
        return this.process
    }

    override fun restartIPython(): Result<Unit, Exception> {
        val rt = this.stopIPython()
            .andThen {
                this.startIPython()
            }
        return rt
    }

    override fun getConnectionFileContent(): KernelConnectionFileContent? {
        return this._connectionFileContent
    }
}
