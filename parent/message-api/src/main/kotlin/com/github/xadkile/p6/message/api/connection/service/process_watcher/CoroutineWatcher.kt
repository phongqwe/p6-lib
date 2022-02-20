package com.github.xadkile.p6.message.api.connection.service.process_watcher

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.xadkile.p6.common.exception.lib.error.CommonErrors
import com.github.xadkile.p6.common.exception.lib.error.ErrorReport
import com.github.xadkile.p6.message.api.connection.service.process_watcher.exception.ProcessWatcherErrors
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking

/**
 * A process watcher that run on a coroutine
 */
sealed class CoroutineWatcher : ProcessWatcher {

    protected var job: Job? = null

    protected fun skeleton(process: Process, block:()->Unit): Result<Unit, ErrorReport>{
        if (this.isWatching().not() && process.isAlive) {
            block()
            return Ok(Unit)
        } else {
            if (this.isWatching()) {
                val report = ErrorReport(
                    type = ProcessWatcherErrors.IllegalState,
                    data =ProcessWatcherErrors.IllegalState.Data(currentState ="Running",correctState ="Not Running")
                )
                return Err(report)
            }
            if (!process.isAlive) {
                return Err(
                    ErrorReport(
                    type =  ProcessWatcherErrors.DeadProcess,
                    data = Unit
                )
                )
            }
        }
        return Err(
            ErrorReport(
            type = CommonErrors.Unknown,
            data = CommonErrors.Unknown.Data("unknow",null)
        )
        )
    }

    override fun stopWatching(): Result<Unit, ErrorReport> {
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
