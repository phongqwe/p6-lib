package com.github.xadkile.bicp.message.api.connection.service.process_watcher

fun interface OnStopEventProcessListener {
    fun onStop(process: Process)

    companion object {
        val nothing = OnStopEventProcessListener {
            // nothing
        }
    }
}
