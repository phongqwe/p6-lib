package com.emeraldblast.p6.message.api.connection.service.heart_beat

import com.emeraldblast.p6.test.utils.TestOnJupyter
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class LiveCountHeartBeatServiceCoroutineTest : TestOnJupyter() {
    lateinit var hbService: LiveCountHeartBeatServiceCoroutine



    @BeforeEach
    fun beforeEach() {
        this.setUp()
        runBlocking{
            kernelContext.startAll()
        }
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
            kernelContext.stopAll()
        }
    }

    @Test
    fun start() {
        runBlocking {
            hbService.start()
            assertTrue(hbService.isServiceRunning())
            hbService.stop()
        }
    }

    @Test
    fun isAlive() = runBlocking {

        hbService.start()
        delay(1000)
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

//    @Test
    fun `test recover from death`(){
        runBlocking {
            val o =hbService.start()
            assertTrue(o is Ok)

            if(o is Err){
                println(o)
            }
            println(o)
            if(!hbService.isServiceRunning()){
                println()
            }

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

