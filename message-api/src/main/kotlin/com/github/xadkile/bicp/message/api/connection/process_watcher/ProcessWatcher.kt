package com.github.xadkile.bicp.message.api.connection.process_watcher

import com.github.michaelbull.result.Result

/**
 * watch a process, do something while watching
 */
interface ProcessWatcher {
    fun startWatching(process:Process):Result<Unit,Exception>
    fun stopWatching():Result<Unit,Exception>
    fun isWatching():Boolean
}


