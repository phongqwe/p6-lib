package com.github.xadkile.bicp.message.api.connection.process_watcher

import com.github.michaelbull.result.Result
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.concurrent.thread

/**
 * Watch a process from a separated thread and react on stdout event.
 */
class OnStdinWatcher(
    private var onStdinListener: OnStdinEventProcessListener = OnStdinEventProcessListener.nothing,
    private var onErrListener: OnErrEventProcessListener = OnErrEventProcessListener.nothing,
) : ProcessWatcher {

    private var threadWatcher: ThreadedWatcher = ThreadedWatcher()

    override fun startWatching(process: Process): Result<Unit, Exception> {
        this.threadWatcher.thread = thread(
            start = false,
            isDaemon = true) {
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            while (process.isAlive) {
                val line = reader.readLine()
                try {
                    if (line == null) {
                        break
                    } else {
                        this.onStdinListener.onStdin(process, line)
                    }
                } catch (e: Exception) {
                    this.onErrListener.onError(process, e)
                    break
                }
            }
        }
        return this.threadWatcher.startWatching(process)
    }

    override fun stopWatching(): Result<Unit, Exception> {
        return this.threadWatcher.stopWatching()
    }

    override fun isWatching(): Boolean {
        return this.threadWatcher.isWatching()
    }

//    override fun isMeaningful(): Boolean {
//        return this.onStdinListener!= OnStdinEventProcessListener.nothing
//    }
}
