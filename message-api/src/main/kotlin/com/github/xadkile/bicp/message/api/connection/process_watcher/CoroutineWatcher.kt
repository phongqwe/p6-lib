package com.github.xadkile.bicp.message.api.connection.process_watcher

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.exception.UnknownException
import kotlinx.coroutines.*

/**
 * A process watcher that run on a separated thread
 */
internal class CoroutineWatcher(
    private val work: (job:Job?) -> Unit,
    private val cScope: CoroutineScope,
    private val cDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ProcessWatcher {
    private var job: Job? = null
    private var process: Process? = null


    override fun startWatching(process: Process): Result<Unit, Exception> {
        if (this.isWatching().not() && process.isAlive) {
            this.process = process
            this.job = cScope.launch(cDispatcher) {
                work(cDispatcher[Job])
            }
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
