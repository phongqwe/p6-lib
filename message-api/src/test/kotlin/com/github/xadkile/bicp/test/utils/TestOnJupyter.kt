package com.github.xadkile.bicp.test.utils

import com.github.xadkile.bicp.message.api.connection.IPythonConfig
import com.github.xadkile.bicp.message.api.connection.IPythonContext
import com.github.xadkile.bicp.message.api.connection.IPythonContextImp
import com.google.gson.Gson
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
    lateinit var ipythonConfig:IPythonConfig
    lateinit var ipythonContext:IPythonContext
    @BeforeAll
    fun before(){
        this.ipythonConfig = TestResource.ipythonConfigForTest()
        this.ipythonContext=IPythonContextImp(this.ipythonConfig)
        this.ipythonContext.startIPython()
        Thread.sleep(2000)
    }

    @AfterAll
    fun afterAll(){
        this.ipythonContext.stopIPython()
    }
}
