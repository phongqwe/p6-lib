package com.github.xadkile.bicp.message.api.connection.process_watcher

import com.github.michaelbull.result.Result
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.concurrent.thread

/**
 * Watch a process from a separated thread and react on StdErr stream
 */
class OnStderrWatcher(
    private var onStdErrListener: OnStdErrEventProcessListener = OnStdErrEventProcessListener.nothing,
    private var onErrListener: OnErrEventProcessListener = OnErrEventProcessListener.nothing,
) : ProcessWatcher {

    private var threadWatcher: ThreadedWatcher = ThreadedWatcher()

    override fun startWatching(process: Process): Result<Unit, Exception> {
        this.threadWatcher.thread = thread(false) {
            val reader = BufferedReader(InputStreamReader(process.errorStream))
            try {
                while (process.isAlive) {
                    try {
                        val line = reader.readLine()
                        if (line == null) {
                            break
                        } else {
                            this.onStdErrListener.onStdErr(process, line)
                        }
                    } catch (e: Exception) {
                        this.onErrListener.onError(process, e)
                    }
                }
            } catch (e: Exception) {
                this.onErrListener.onError(process, e)
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
//        return this.onStdErrListener != OnStdErrEventProcessListener.nothing
//    }
}
