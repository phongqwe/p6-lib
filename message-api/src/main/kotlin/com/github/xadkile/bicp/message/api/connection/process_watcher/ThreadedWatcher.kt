package com.github.xadkile.bicp.message.api.connection.process_watcher

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.exception.ProcessWatcherIllegalState
import com.github.xadkile.bicp.message.api.exception.UnknownException

/**
 * A process watcher that run on a separated thread
 */
class ThreadedWatcher: ProcessWatcher {
    var thread: Thread?= null
    private var process: Process? = null
    private var isWatching: Boolean = false

    override fun startWatching(process: Process): Result<Unit, Exception> {
        if (isWatching.not() && process.isAlive && this.thread!=null) {
            this.process = process
            this.thread?.start()
            this.isWatching = true
            return Ok(Unit)
        } else {
            if (this.isWatching) return Err(ProcessWatcherIllegalState("Process watcher is already running"))
            if (!process.isAlive) return Err(ProcessWatcherIllegalState("Cannot watch dead process"))
        }
        return Err(UnknownException("OnStopWatcher: impossible"))
    }

    override fun stopWatching(): Result<Unit, Exception> {
        if (this.isWatching) {
            this.thread?.interrupt()
            this.isWatching = false
            return Ok(Unit)
        } else {
            //do nothing
            return Ok(Unit)
        }
    }

    override fun isWatching(): Boolean {
        return this.isWatching
    }
}
