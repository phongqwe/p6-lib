package com.github.xadkile.bicp.message.api.connection.service.heart_beat.coroutine

import com.github.michaelbull.result.unwrap
import com.github.xadkile.bicp.test.utils.TestOnJupyter
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class LiveCountHeartBeatServiceCoroutineTest : TestOnJupyter() {
    lateinit var hbService: LiveCountHeartBeatServiceCoroutine

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun beforeEach() {
        this.kernelContext.startKernel()
        hbService = LiveCountHeartBeatServiceCoroutine(
            zcontext,
            this.kernelContext.getSocketProvider().unwrap(),
            3,
            1000,
            TestCoroutineScope(),
            mainThreadSurrogate
        )
    }

    @AfterEach
    fun afterEach() {
        hbService.stop()
        runBlocking {
            kernelContext.stopKernel()
        }
    }

    @Test
    fun start() {
        hbService.start()
        assertTrue(hbService.isServiceRunning())
    }

    @Test
    fun isAlive() {
        this.kernelContext.startKernel()
        hbService.start()
        Thread.sleep(1000)
        assertTrue(hbService.isHBAlive())
    }

    @Test
    fun stop() {
        hbService.start()
        hbService.stop()
        assertFalse(hbService.isServiceRunning())
    }
}
