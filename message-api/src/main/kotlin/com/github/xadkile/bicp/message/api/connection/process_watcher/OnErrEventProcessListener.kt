package com.github.xadkile.bicp.message.api.connection.process_watcher

fun interface OnErrEventProcessListener {
    fun onError(process: Process, throwable: Throwable)

    companion object {
        val nothing = OnErrEventProcessListener { process, throwable ->
            // nothing
        }
    }
}
