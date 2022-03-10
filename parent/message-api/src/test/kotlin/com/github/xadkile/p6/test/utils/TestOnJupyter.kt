package com.github.xadkile.p6.test.utils

import com.github.xadkile.p6.message.api.connection.kernel_context.*
import kotlinx.coroutines.*
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
    lateinit var kernelContext: KernelContext
    lateinit var iPythonContextConv: KernelContextReadOnly
    lateinit var zcontext: ZContext

    @BeforeAll
    open fun beforeAll() {
        this.zcontext = ZContext()
        this.ipythonConfig = TestResources.kernelConfigForTest()
        this.kernelContext = KernelContextImp(this.ipythonConfig, zcontext, GlobalScope, Dispatchers.IO)
        this.iPythonContextConv = this.kernelContext
        runBlocking {
            kernelContext.startAll()
        }
    }
    @AfterAll
    fun afterAll(){
        runBlocking {
            kernelContext.stopAll()
        }
    }

    fun newKernelContext():KernelContext{
        val rt = KernelContextImp(this.ipythonConfig, zcontext, GlobalScope, Dispatchers.IO)
        return rt
    }
}
