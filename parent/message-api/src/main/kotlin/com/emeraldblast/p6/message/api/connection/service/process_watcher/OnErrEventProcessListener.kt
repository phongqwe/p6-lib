package com.emeraldblast.p6.message.api.connection.service.process_watcher

fun interface OnErrEventProcessListener {
    fun onError(process: Process, throwable: Throwable)

    companion object {
        val nothing = OnErrEventProcessListener { process, throwable ->
            // nothing
        }
    }
}
