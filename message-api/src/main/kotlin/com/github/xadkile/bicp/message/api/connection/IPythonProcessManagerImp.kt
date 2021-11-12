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

// TODO test this
class IPythonProcessManagerImp @Inject constructor(private val ipythonConfig: IPythonConfig) : IPythonProcessManager {

    private val launchCmd: List<String> by lazy {
        this.ipythonConfig.makeLaunchCmmd()
    }

    private var process: Process? = null
    private var _connectionFileContent: KernelConnectionFileContent? = null
    private var connectionFilePath: Path? = null

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
                return rt
            } catch (e: Exception) {
                return Err(e)
            }
        }
    }

    override fun stopIPython(): Result<Unit, Exception> {
        if (this.isRunning().not()) {
            return Ok(Unit)
        }
        try {
            // clear objects relate to connection
            if (this.process != null) {
                this.process?.onExit()?.thenApply {
                    this._connectionFileContent = null
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
                        this.connectionFilePath = null
                    }
                }
                this.process?.destroy()
                runBlocking {
                    // wait until the process is dead completely
                    while (this@IPythonProcessManagerImp.process?.isAlive == true) {
                        delay(50)
                    }
                }
                this.process = null
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

    override fun getConnectionFileContent(): KernelConnectionFileContent? {
        return this._connectionFileContent
    }

    private fun isRunning(): Boolean {
        return (this.process?.isAlive
            ?: false) && this.connectionFilePath != null && this._connectionFileContent != null
    }
}
