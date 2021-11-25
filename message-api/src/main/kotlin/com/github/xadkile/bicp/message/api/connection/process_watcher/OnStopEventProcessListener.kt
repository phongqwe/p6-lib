package com.github.xadkile.bicp.message.api.connection.process_watcher

fun interface OnStopEventProcessListener {
    fun onStop(process: Process)

    companion object {
        val nothing = OnStopEventProcessListener {
            // nothing
        }
    }
}
