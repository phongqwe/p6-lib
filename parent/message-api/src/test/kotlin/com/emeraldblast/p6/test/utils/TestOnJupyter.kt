package com.emeraldblast.p6.test.utils

import com.emeraldblast.p6.message.api.connection.kernel_context.*
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
    lateinit var zcontext: ZContext


    fun setUp(){
        this.zcontext = ZContext()
        this.ipythonConfig = TestResources.kernelConfigForTest()
        this.kernelContext = KernelContextImp(this.ipythonConfig, zcontext, GlobalScope, Dispatchers.IO)
    }
}
