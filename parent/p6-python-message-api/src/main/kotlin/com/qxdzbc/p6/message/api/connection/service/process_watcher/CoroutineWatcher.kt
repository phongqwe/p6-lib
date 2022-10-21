package com.qxdzbc.p6.message.api.connection.service.process_watcher

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.qxdzbc.common.error.CommonErrors
import com.qxdzbc.common.error.ErrorReport
import com.qxdzbc.p6.message.api.connection.service.process_watcher.exception.ProcessWatcherErrors
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
                    header = ProcessWatcherErrors.IllegalState.header,
                    data =ProcessWatcherErrors.IllegalState.Data(currentState ="Running",correctState ="Not Running")
                )
                return Err(report)
            }
            if (!process.isAlive) {
                return Err(
                    ErrorReport(
                    header =  ProcessWatcherErrors.DeadProcess.header,
                    data = Unit
                )
                )
            }
        }
        return Err(
            ErrorReport(
            header = CommonErrors.Unknown.header,
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
