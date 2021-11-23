package com.github.xadkile.bicp.message.api.connection.heart_beat.coroutine

import com.github.michaelbull.result.unwrap
import com.github.xadkile.bicp.test.utils.TestOnJupyter
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.coroutines.CoroutineContext

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class LiveCountHeartBeatServiceCoroutineTest : TestOnJupyter() {
    lateinit var hbService: LiveCountHeartBeatServiceCoroutine
    lateinit var mainThreadSurrogate:ExecutorCoroutineDispatcher

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun beforeEach() {
        this.ipythonContext.startIPython()
        hbService = LiveCountHeartBeatServiceCoroutine(
            zcontext, this.ipythonContext.getSocketProvider().unwrap(), 3, 1000, TestCoroutineScope(),mainThreadSurrogate
        )
    }

    @BeforeAll
    override fun beforeAll(){
        super.beforeAll()
        mainThreadSurrogate=newSingleThreadContext("Test Thread")
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @AfterAll
    override fun afterAll(){
        super.afterAll()
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }
    @AfterEach
    fun afterEach() {
        hbService.stop()
        ipythonContext.stopIPython()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun start() {
        ipythonContext.startIPython()
        runBlockingTest {
            hbService.start()
        }
        assertTrue(hbService.isServiceRunning())
    }

    @Test
    fun isAlive() {
        this.ipythonContext.startIPython()
        runBlockingTest {
            hbService.start()
        }
        assertTrue(hbService.isServiceRunning())
        Thread.sleep(1000)
        assertTrue(hbService.isHBAlive())
    }

    @Test
    fun stop() {
        ipythonContext.startIPython()
        hbService.start()
        hbService.stop()
        assertFalse(hbService.isServiceRunning())
    }
}
