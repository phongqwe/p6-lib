package com.github.xadkile.bicp.message.api.connection.service.process_watcher

fun interface OnStdinEventProcessListener {
    fun onStdin(process: Process, content: String)

    companion object {
        val nothing = OnStdinEventProcessListener { process, content ->
            // nothing
        }
    }
}
