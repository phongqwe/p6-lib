package com.emeraldblast.p6.message.api.connection.service.process_watcher

import com.github.michaelbull.result.Result
import com.emeraldblast.p6.common.exception.error.ErrorReport
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Watch a process from a separated thread and react on StdErr stream
 */
class OnStderrWatcher(
    private var onStdErrListener: OnStdErrEventProcessListener = OnStdErrEventProcessListener.nothing,
    private var onErrListener: OnErrEventProcessListener = OnErrEventProcessListener.nothing,
    private val cScope: CoroutineScope,
    private val cDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : CoroutineWatcher() {

    override fun startWatching(process: Process): Result<Unit, ErrorReport> {
        return this.skeleton(process){
            this.job = cScope.launch(cDispatcher) {
                val reader = BufferedReader(InputStreamReader(process.errorStream))
                while(isActive){
                    while (process.isAlive) {
                        try {
                            val line = reader.readLine()
                            if (line == null) {
                                break
                            } else {
                                onStdErrListener.onStdErr(process, line)
                            }
                        } catch (e: Exception) {
                            onErrListener.onError(process, e)
                        }
                    }
                }
            }
        }
    }
}
