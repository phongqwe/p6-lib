package com.github.xadkile.p6.message.api.connection.service.heart_beat

import com.github.michaelbull.result.unwrap
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
        runBlocking {
            kernelContext.startKernel()
        }
        hbService = LiveCountHeartBeatServiceCoroutine(
            zcontext,
            kernelContext.getSocketProvider().unwrap(),
            liveCount = 3,
            pollTimeout = 1000,
            startTimeOut = 5000,
            cScope = GlobalScope,
            cDispatcher = Dispatchers.IO
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
                hbService.start()
                assertTrue(hbService.isServiceRunning())
            }
        }

        @Test
        fun isAlive() = runBlocking {
            kernelContext.startAll()
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
    }

