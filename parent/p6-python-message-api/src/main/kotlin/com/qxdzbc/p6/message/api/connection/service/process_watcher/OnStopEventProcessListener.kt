package com.qxdzbc.p6.message.api.connection.service.process_watcher

fun interface OnStopEventProcessListener {
    fun onStop(process: Process)

    companion object {
        val nothing = OnStopEventProcessListener {
            // nothing
        }
    }
}
