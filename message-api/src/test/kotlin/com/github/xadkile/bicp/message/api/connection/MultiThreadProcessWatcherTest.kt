package com.github.xadkile.bicp.message.api.connection

import com.github.xadkile.bicp.message.api.connection.process_watcher.MultiThreadProcessWatcher
import org.junit.jupiter.api.Test

internal class MultiThreadProcessWatcherTest {

    @Test
    fun startWatching() {
        val watcher = MultiThreadProcessWatcher(
            onStdoutListener = { process, content ->
                println(content)
                               },
            onErrListener = {p,e-> println(e)},
            onStopListener = {p-> println("STOPPPPPPP:${p.exitValue()}")},
            onStdErrListener = {p,c-> println(c)}
        )
        val p = ProcessBuilder("/home/abc/Applications/anaconda3/envs/dl_hw_01/bin/python", "-m", "ipykernel_launcher", "-f","/tmp/phong-kernel4.json").start()


        watcher.startWatching(p)
        Thread.sleep(10000)
        p.destroy()
//        watcher.stopWatching()
        Thread.sleep    (10000)
    }

}
