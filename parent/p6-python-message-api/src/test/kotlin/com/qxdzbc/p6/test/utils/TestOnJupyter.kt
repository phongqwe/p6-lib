package com.qxdzbc.p6.test.utils

import com.qxdzbc.p6.message.api.connection.kernel_context.*
import com.qxdzbc.p6.message.api.connection.kernel_services.KernelServiceManager
import com.qxdzbc.p6.message.di.DaggerMessageApiComponent
import kotlinx.coroutines.*
import org.zeromq.ZContext

/**
 * This test class will start jupyter before running any test, and destroy jupyter process after all of its tests are done.
 * Extend this, then add my test as normal. Use [connectionFileContent] to get a connection file content
 */
abstract class TestOnJupyter {
    lateinit var kernelConfig: KernelConfig
    lateinit var kernelContext: KernelContext
    lateinit var zcontext: ZContext
    lateinit var kernelServiceManager: KernelServiceManager


    fun setUp(){
        this.zcontext = ZContext()
        this.kernelConfig = TestResources.kernelConfigForTest()
        val dgComp = DaggerMessageApiComponent.builder()
            .kernelConfig(this.kernelConfig)
            .kernelCoroutineScope(GlobalScope)
            .networkServiceCoroutineDispatcher(Dispatchers.IO)
            .build()
        this.kernelContext = dgComp.kernelContext()
        this.kernelServiceManager = dgComp.kernelServiceManager()
    }
}
