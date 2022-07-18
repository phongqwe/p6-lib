package com.emeraldblast.p6.message.api.connection.service.heart_beat

import com.emeraldblast.p6.test.utils.TestOnJupyter
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import kotlinx.coroutines.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class LiveCountHeartBeatServiceTest : TestOnJupyter() {
    lateinit var hbService: LiveCountHeartBeatService

    @BeforeEach
    fun beforeEach() {
        this.setUp()
        runBlocking{
            kernelContext.startAll()
        }
        hbService = LiveCountHeartBeatService(
            kernelContext=kernelContext,
            liveCount = 3,
            pollTimeOut = 1000,
            startTimeOut = 5000,
            coroutineScopeX = GlobalScope,
            dispatcher = Dispatchers.IO
        )
    }

    @AfterEach
    fun afterEach() {
        runBlocking {
            hbService.stop()
            kernelContext.stopAll()
        }
    }

    @Test
    fun start() {
        runBlocking {
            val startRs=hbService.start()
            assertTrue { startRs is Ok }
            assertTrue(hbService.isServiceRunning())
            hbService.stop()
        }
    }

    @Test
    fun isAlive() = runBlocking {
        hbService.start()
        val rt = hbService.waitHBALive()
        assertTrue { rt is Ok }
        assertTrue(hbService.isHBAlive())
    }

//    @Test
    fun `turning kernel on-off while hb service is running`(): Unit = runBlocking {
        hbService.start()
        val rt = hbService.waitHBALive()
        assertTrue { rt is Ok }
        assertTrue(hbService.isHBAlive())

        // turn off kernel
        kernelContext.stopAll()
        // wait for the live count to run out
        delay(3000)
        assertFalse(hbService.isHBAlive())
        launch (Dispatchers.IO){
            val rt2 = hbService.waitHBALive()
            assertTrue { rt2 is Ok }
        }
        // wait a bit
        delay(1000)
        // turn on kernel
        kernelContext.startAll()
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
}

