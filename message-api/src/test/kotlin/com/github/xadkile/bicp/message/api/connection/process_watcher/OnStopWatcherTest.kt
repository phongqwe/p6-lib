package com.github.xadkile.bicp.message.api.connection.process_watcher

import com.github.michaelbull.result.Ok
import com.github.xadkile.bicp.message.api.connection.heart_beat.coroutine.LiveCountHeartBeatServiceCoroutine
import com.github.xadkile.bicp.test.utils.TestOnRBCoroutine
import com.github.xadkile.bicp.test.utils.TestResources
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.*
import kotlin.test.assertFalse
import kotlin.test.assertTrue
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class OnStopWatcherTest : TestOnRBCoroutine() {

    @Test
    fun test_StopWatcherInTheMiddle() {
        runBlocking {
            var flag = false
            val watcher = OnStopWatcher(
                onStopListener = { p ->
                    flag = true
                    println("TEST_LABEL:${p.exitValue()}")
                },
                cScope = this,
                cDispatcher = mainThreadSurrogate
            )
            val p = ProcessBuilder(TestResources.dummyProcessCmd(10)).inheritIO().start()
            assertFalse(watcher.isWatching())
            val r = watcher.startWatching(p)
            assertTrue(r is Ok, r.toString())
            assertTrue(watcher.isWatching())
            Thread.sleep(1000)
            watcher.stopWatching()
            assertFalse(flag,"Listener should not be triggered")
            assertFalse(watcher.isWatching())
        }
    }

    @Test
    fun test_StopProcessInTheMiddle() {
        runBlocking {
            var flag = false
            val watcher = OnStopWatcher(
                onStopListener = { p ->
                    flag = true
                    println("TEST_LABEL:${p.exitValue()}")
                },
                cScope = this,
                cDispatcher = mainThreadSurrogate
            )
            val p = ProcessBuilder(TestResources.dummyProcessCmd(10)).inheritIO().start()
            assertFalse(watcher.isWatching())
            val r = watcher.startWatching(p)
            assertTrue(r is Ok, r.toString())
            assertTrue(watcher.isWatching())
            Thread.sleep(1000)
            p.destroy()
            Thread.sleep(1000)
            assertTrue(flag,"Listener should be triggered")
            assertFalse(watcher.isWatching())
        }
    }

    @Test
    fun test_WaitUntilTheEndOfProcess() {
        runBlocking {
            var flag = false
            val watcher = OnStopWatcher(
                onStopListener = { p ->
                    flag = true
                    println("TEST_LABEL:${p.exitValue()}")
                },
                cScope = this,
                cDispatcher = mainThreadSurrogate
            )
            val p = ProcessBuilder(TestResources.dummyProcessCmd(1)).inheritIO().start()
            assertFalse(watcher.isWatching())
            val r = watcher.startWatching(p)
            assertTrue(r is Ok, r.toString())
            assertTrue(watcher.isWatching())
            Thread.sleep(2000)
            assertTrue(flag,"Listener should be triggered")
            assertFalse(watcher.isWatching())
        }
    }
}
