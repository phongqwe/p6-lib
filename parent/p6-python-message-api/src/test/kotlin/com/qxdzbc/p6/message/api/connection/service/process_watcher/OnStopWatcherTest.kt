package com.qxdzbc.p6.message.api.connection.service.process_watcher

import com.github.michaelbull.result.Ok
import com.qxdzbc.p6.test.utils.TestOnRBCoroutine
import com.qxdzbc.p6.test.utils.TestResources
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class OnStopWatcherTest : TestOnRBCoroutine() {
    lateinit var p: Process

    @BeforeEach
    fun bee(){
        p = ProcessBuilder(TestResources.dummyProcessCmd(5)).inheritIO().start()
    }

    @AfterEach
    fun ae(){
        if(p.isAlive){
            p.destroy()
        }
    }


    @Test
    fun test_StopWatcherInTheMiddle() {

        runBlocking {
            var flag = 0
            val watcher = OnStopWatcher(
                onStopListener = { p ->
                    flag += 1
                    println("TEST_LABEL:${p.exitValue()}")
                },
                cScope = this,
                cDispatcher = mainThreadSurrogate
            )

            assertFalse(watcher.isWatching())
            val r = watcher.startWatching(p)
            assertTrue(r is Ok, r.toString())
            assertTrue(watcher.isWatching())
            watcher.stopWatching()
            Thread.sleep(1000)
            assertEquals(0, flag, "Listener should NOT be triggered ")
            assertFalse(watcher.isWatching())

        }
        p.destroy()
    }

    @Test
    fun test_StopProcessInTheMiddle() {
        runBlocking {
            var flag = 0
            val watcher = OnStopWatcher(
                onStopListener = { p ->
                    flag += 1
                    println("TEST_LABEL:${p.exitValue()}")
                },
                cScope = this,
                cDispatcher = mainThreadSurrogate
            )

            val r = watcher.startWatching(p)
            assertTrue(r is Ok, r.toString())
            assertTrue(watcher.isWatching())
            Thread.sleep(1000)
            p.destroy()
            Thread.sleep(1000)
            assertEquals(1, flag, "Listener should be triggered exactly 1")
            assertTrue(watcher.isWatching())
            watcher.stopWatching()
        }
    }

    @Test
    fun test_WaitUntilTheEndOfProcess() {
        runBlocking {
            var flag = 0
            val watcher = OnStopWatcher(
                onStopListener = { p ->
                    flag += 1
                    println("TEST_LABEL:${p.exitValue()}")
                },
                cScope = this,
                cDispatcher = mainThreadSurrogate
            )

            assertFalse(watcher.isWatching())
            val r = watcher.startWatching(p)
            assertTrue(r is Ok, r.toString())
            assertTrue(watcher.isWatching())
            Thread.sleep(6000)
            assertEquals(1, flag, "Listener should be triggered exactly 1")
            assertTrue(watcher.isWatching())
            watcher.stopWatching()
        }
    }
}
