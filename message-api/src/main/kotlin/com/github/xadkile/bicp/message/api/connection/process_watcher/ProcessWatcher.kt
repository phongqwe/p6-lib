package com.github.xadkile.bicp.message.api.connection.process_watcher

import com.github.michaelbull.result.Result

/**
 * watch a process, do something while watching
 */
interface ProcessWatcher {
    /**
     * start watching.
     * return Err if this function is called on already started watcher
     */
    fun startWatching(process:Process):Result<Unit,Exception>

    /**
     * stop watching. Calling stopWatching() on already stopped watcher should not have any affect and return Ok.
     */
    fun stopWatching():Result<Unit,Exception>
    fun isWatching():Boolean
}


