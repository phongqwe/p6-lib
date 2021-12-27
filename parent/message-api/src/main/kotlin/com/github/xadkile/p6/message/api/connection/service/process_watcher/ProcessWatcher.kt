package com.github.xadkile.p6.message.api.connection.service.process_watcher

import com.github.michaelbull.result.Result
import com.github.xadkile.p6.exception.error.ErrorReport

/**
 * watch a process, do something while watching.
 * ProcessWatcher's lifecycle does not tie to the process it is watching.
 * If a process die during the watching process, the watcher will continue doing its job.
 */
interface ProcessWatcher {
    /**
     * start watching.
     * return Err if this function is called on already started watcher
     */
    fun startWatching(process:Process):Result<Unit,ErrorReport>

    /**
     * stop watching. Calling stopWatching() on already stopped watcher should not have any affect and return Ok.
     */
    fun stopWatching():Result<Unit,ErrorReport>
    fun isWatching():Boolean
}


