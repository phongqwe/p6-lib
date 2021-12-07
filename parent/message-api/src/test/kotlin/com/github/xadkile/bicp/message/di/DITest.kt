package com.github.xadkile.bicp.message.di

import com.github.michaelbull.result.Ok
import com.github.xadkile.bicp.test.utils.TestResources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class DITest {
    @Test
    fun test(){
        runBlocking {
            val component = DaggerMessageApiComponent
                .builder()
                .kernelConfig(TestResources.kernelConfigForTest())
                .applicationCoroutineScope(GlobalScope)
                .networkServiceCoroutineDispatcher(Dispatchers.IO)
                .build()

            val context = component.ipythonContext()

            context.startAll()
            assertTrue(context.getChannelProvider() is Ok)
            context.stopAll()
        }
    }
}
