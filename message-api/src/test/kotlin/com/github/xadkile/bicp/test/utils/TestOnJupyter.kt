package com.github.xadkile.bicp.test.utils

import com.github.xadkile.bicp.message.api.connection.ipython_context.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.zeromq.ZContext

/**
 * This test class will start jupyter before running any test, and destroy jupyter process after all of its tests are done.
 * Extend this, then add my test as normal. Use [connectionFileContent] to get a connection file content
 */
abstract class TestOnJupyter {
    lateinit var ipythonConfig: KernelConfig
    lateinit var ipythonContext: IPythonContext
    lateinit var iPythonContextConv: IPythonContextReadOnlyConv
    lateinit var zcontext:ZContext
    lateinit var mainThreadSurrogate: ExecutorCoroutineDispatcher
    @BeforeAll
    open fun beforeAll(){
        this.zcontext = ZContext()
        this.ipythonConfig = TestResources.ipythonConfigForTest()
        this.ipythonContext= IPythonContextImp(this.ipythonConfig,zcontext)
        this.iPythonContextConv = this.ipythonContext.conv()
        mainThreadSurrogate= newSingleThreadContext("Test Thread")
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @AfterAll
    open fun afterAll(){
        this.ipythonContext.stopIPython()
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }
}
