package com.github.xadkile.bicp.message.api.connection.service.process_watcher

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.connection.service.process_watcher.exception.ProcessWatcherIllegalStateException
import com.github.xadkile.bicp.exception.UnknownException
import kotlinx.coroutines.*

/**
 * A process watcher that run on a coroutine
 */
sealed class CoroutineWatcher : ProcessWatcher {

    protected var job: Job? = null

    protected fun skeleton(process: Process, block:()->Unit): Result<Unit, Exception>{
        if (this.isWatching().not() && process.isAlive) {
            block()
            return Ok(Unit)
        } else {
            if (this.isWatching()) return Err(ProcessWatcherIllegalStateException("Process watcher is already running"))
            if (!process.isAlive) return Err(ProcessWatcherIllegalStateException("Cannot watch dead process"))
        }
        return Err(UnknownException("OnStopWatcher: impossible"))
    }

    override fun stopWatching(): Result<Unit, Exception> {
        if (this.isWatching()) {
            runBlocking {
                job?.cancel()
                this@CoroutineWatcher.job?.join()
            }
        }
        return Ok(Unit)
    }

    override fun isWatching(): Boolean {
        return this.job?.isActive == true
    }
}
