package com.github.xadkile.p6.message.api.connection.service.heart_beat

import com.github.xadkile.p6.test.utils.TestOnJupyter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class LiveCountHeartBeatServiceCoroutineTest : TestOnJupyter() {
    lateinit var hbService: LiveCountHeartBeatServiceCoroutine

    @BeforeEach
    fun beforeEach() {
        hbService = LiveCountHeartBeatServiceCoroutine(
            kernelContext=kernelContext,
            liveCount = 3,
            pollTimeOut = 1000,
            startTimeOut = 5000,
            coroutineScope = GlobalScope,
            dispatcher = Dispatchers.IO
        )
    }

    @AfterEach
    fun afterEach() {
        runBlocking {
            hbService.stop()
        }
    }

    @Test
    fun start() {
        runBlocking {
            kernelContext.startAll()
            hbService.start()
            assertTrue(hbService.isServiceRunning())
        }
    }

    @Test
    fun isAlive() = runBlocking {
        hbService.start()
        Thread.sleep(1000)
        assertTrue(hbService.isHBAlive())
    }

    @Test
    fun stop() {
        runBlocking {
            hbService.start()
            delay(1000)
            hbService.stop()
            assertFalse(hbService.isServiceRunning())
        }
    }

    @Test
    fun testDeadThenAlive(){
        runBlocking {
            hbService.start()
            assertTrue(hbService.isServiceRunning())
            assertTrue(hbService.isHBAlive())
            kernelContext.stopAll()
            delay(6000)
            assertTrue(hbService.isServiceRunning())
            assertFalse(hbService.isHBAlive())
            kernelContext.startAll()
            delay(2000)
            assertTrue(hbService.isServiceRunning())
            assertTrue(hbService.isHBAlive())
        }
    }
}

