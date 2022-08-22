package com.qxdzbc.p6.message.api.connection.service.process_watcher

import com.github.michaelbull.result.Result
import com.qxdzbc.p6.common.exception.error.ErrorReport
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Watch a process from a separated thread and react on stdout event.
 */
class OnStdinWatcher(
    private var onStdinListener: OnStdinEventProcessListener = OnStdinEventProcessListener.nothing,
    private var onErrListener: OnErrEventProcessListener = OnErrEventProcessListener.nothing,
    private val cScope: CoroutineScope,
    private val cDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : CoroutineWatcher() {

    override fun startWatching(process: Process): Result<Unit, ErrorReport> {

        return this.skeleton(process) {
            this.job = cScope.launch(cDispatcher) {
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                while (this.isActive) {
                    while (process.isAlive) {
                        val line: String? = reader.readLine()
                        try {
                            if (line == null) {
                                break
                            } else {
                                onStdinListener.onStdin(process, line)
                            }
                        } catch (e: Exception) {
                            onErrListener.onError(process, e)
                            break
                        }
                    }
                }
            }
        }
    }
}
