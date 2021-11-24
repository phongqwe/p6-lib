package com.github.xadkile.bicp.message.api.connection.process_watcher

import com.github.michaelbull.result.Ok
import com.github.xadkile.bicp.test.utils.TestOnRBCoroutine
import com.github.xadkile.bicp.test.utils.TestResources
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertFalse

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class OnStdinWatcherTest : TestOnRBCoroutine() {

    @Test
    fun startWatching() {
        runBlocking {
            var flag = false
            val watcher = OnStdinWatcher(
                onStdinListener = { p,c ->
                    flag = true
                    println("TEST_LABEL:${c}")
                },
                cScope = this,
                cDispatcher = mainThreadSurrogate
            )
            val p = ProcessBuilder(TestResources.dummyProcessCmd(5)).start()
            Thread.sleep(100)
            assertFalse(watcher.isWatching())
            val r = watcher.startWatching(p)
            kotlin.test.assertTrue(r is Ok,r.toString())
            kotlin.test.assertTrue(watcher.isWatching())
            Thread.sleep(5000)
            p.destroy()
            Thread.sleep(5000)
            kotlin.test.assertTrue(watcher.isWatching())
            watcher.stopWatching()
            kotlin.test.assertTrue(flag)
            kotlin.test.assertFalse(watcher.isWatching())
        }

    }
}
