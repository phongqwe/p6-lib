package com.github.xadkile.p6.message.api.connection.service.process_watcher

import com.github.michaelbull.result.Result
import com.github.xadkile.p6.common.exception.lib.error.ErrorReport
import kotlinx.coroutines.*

/**
 * Watch a process from a separated thread and react when the process stops.
 * This watcher can actually catch the kill signal from outside this app.
 * the listener only triggers on exit event of the process.
 * If the watcher is stopped midway, the listener must NOT trigger, and the process must not be affected
 */
class OnStopWatcher(
    private var onStopListener: OnStopEventProcessListener = OnStopEventProcessListener.nothing,
    private val cScope: CoroutineScope,
    private val cDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : CoroutineWatcher() {
    override fun startWatching(process: Process): Result<Unit, ErrorReport> {

        return this.skeleton(process){
            this.job = cScope.launch(cDispatcher) {
                var triggered = false
                while (isActive) {
                    if (!process.isAlive && !triggered) {
                        onStopListener.onStop(process)
                        triggered = true
                    }
                }
            }
        }
    }
}
