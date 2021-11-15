package com.github.xadkile.bicp.message.api.connection.process_watcher

import com.github.michaelbull.result.Result


/**
 * watch a process
 */
interface ProcessWatcher {
    fun startWatching(process: Process): Result<Unit, Exception>
    fun stopWatching(): Result<Unit, Exception>
    fun setStopEventListener(listener: OnStopEventProcessListener)
    fun setStdOutEventListener(listener: OnStdOutEventProcessListener)
    fun setErrEventProcessListener(listener: OnErrEventProcessListener)
    fun setOnStdErrEventProcessListener(listener: OnStdErrEventProcessListener)
}


