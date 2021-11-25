package com.github.xadkile.bicp.message.api.connection.process_watcher

fun interface OnStdinEventProcessListener {
    fun onStdin(process: Process, content: String)

    companion object {
        val nothing = OnStdinEventProcessListener { process, content ->
            // nothing
        }
    }
}
