package com.github.xadkile.bicp.message.api.connection.process_watcher

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread

/**
 * Watch a process from a separated thread and react when the process stops.
 * This watcher can actually catch the kill signal from outside this app.
 */
class OnStopWatcher(
    private var onStopListener: OnStopEventProcessListener = OnStopEventProcessListener.nothing,
    private var onErrListener: OnErrEventProcessListener = OnErrEventProcessListener.nothing,
) : ProcessWatcher {

    private var threadWatcher: ThreadedWatcher = ThreadedWatcher()

    override fun startWatching(process: Process): Result<Unit, Exception> {
        this.threadWatcher.thread = thread(false) {
            try {
                process.waitFor()
                this.onStopListener.onStop(process)
            } catch (e: InterruptedException) {
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
}
