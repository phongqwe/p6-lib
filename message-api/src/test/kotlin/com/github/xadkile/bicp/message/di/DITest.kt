package com.github.xadkile.bicp.message.di

import com.github.michaelbull.result.Ok
import com.github.xadkile.bicp.test.utils.TestResources
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class DITest {
    @Test
    fun test(){
        val component = DaggerMessageApiComponent
            .builder()
            .ipythonConfig(TestResources.ipythonConfigForTest())
            .build()

        val context = component.ipythonContext()

        context.startIPython()
        assertTrue(context.getChannelProvider() is Ok)
        context.stopIPython()
    }
}
