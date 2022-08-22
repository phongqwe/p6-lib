package com.qxdzbc.p6.message.api.connection.service.process_watcher

import com.github.michaelbull.result.Ok
import com.qxdzbc.p6.test.utils.TestOnRBCoroutine
import com.qxdzbc.p6.test.utils.TestResources
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class OnStderrWatcherTest : TestOnRBCoroutine(){

    @Test
    fun testTheWholeLifeCycle() {
        runBlocking {
            var flag = false
            val watcher = OnStderrWatcher(
                onStdErrListener = { p,c ->
                    flag = true
                    println("TEST_LABEL:${c}")
                },
                cScope = this,
                cDispatcher = mainThreadSurrogate
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
}
