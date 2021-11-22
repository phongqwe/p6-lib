package com.github.xadkile.bicp.message.api.connection.process_watcher

import com.github.michaelbull.result.Ok
import com.github.xadkile.bicp.test.utils.TestResources
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class OnStopWatcherTest {

    @Test
    fun testTheWholeLifeCycle() {
        var flag = false
        val watcher = OnStopWatcher(
            onStopListener = { p ->
                flag = true
                println("STOP:${p.exitValue()}")
            }
        )
        val p = ProcessBuilder(TestResources.dummyProcessCmd(5)).inheritIO().start()
        assertFalse(watcher.isWatching())
        val r = watcher.startWatching(p)
        assertTrue(r is Ok)
        assertTrue(watcher.isWatching())
        Thread.sleep(5000)
        p.destroy()
        Thread.sleep(5000)
        assertTrue(watcher.isWatching())
        watcher.stopWatching()
        assertTrue(flag)
        assertFalse(watcher.isWatching())
    }
}
