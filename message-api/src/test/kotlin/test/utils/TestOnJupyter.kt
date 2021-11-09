package test.utils

import com.google.gson.Gson
import com.github.xadkile.bicp.message.api.protocol.KernelConnectionFileContent
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths

/**
 * This test class will start jupyter before running any test, and destroy jupyter process after all of its tests are done.
 * Extend this, then add my test as normal. Use [connectionFileContent] to get a connection file content
 */
abstract class TestOnJupyter {
    lateinit var process:Process
    lateinit var jpConfig:JupyterTestConfig
    lateinit var connectionFileContent: KernelConnectionFileContent
    @BeforeAll
    fun before(){
        this.jpConfig = JupyterTestConfig.fromFile()
        val processBuilder = ProcessBuilder(this.jpConfig.makeCmd())
        this.process = processBuilder.inheritIO().start()
        Thread.sleep(2000)
        this.connectionFileContent = this.jpConfig.connectionFile()
        Thread.sleep(1000)
    }

    @AfterAll
    fun afterAll(){
        this.process.destroy()
    }
}
