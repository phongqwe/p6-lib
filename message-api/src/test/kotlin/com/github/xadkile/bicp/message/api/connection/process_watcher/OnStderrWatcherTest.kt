package com.github.xadkile.bicp.message.api.connection.process_watcher

import com.github.michaelbull.result.Ok
import com.github.xadkile.bicp.test.utils.TestResources
import org.junit.jupiter.api.Test

internal class OnStderrWatcherTest {

    @Test
    fun testTheWholeLifeCycle() {
        var flag = false
        val watcher = OnStderrWatcher(
            onStdErrListener = { p,c ->
                flag = true
                println("STD_ERR:${c}")
            }
        )
        val p = ProcessBuilder(TestResources.dummyProcessCmd(5)).start()
        Thread.sleep(100)
        kotlin.test.assertFalse(watcher.isWatching())
        val r = watcher.startWatching(p)
        kotlin.test.assertTrue(r is Ok)
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
