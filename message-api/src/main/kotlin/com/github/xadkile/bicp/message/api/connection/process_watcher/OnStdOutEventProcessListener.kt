package com.github.xadkile.bicp.message.api.connection.process_watcher

fun interface OnStdOutEventProcessListener {
    fun onStdOut(process: Process, content: String)

    companion object {
        val nothing = OnStdOutEventProcessListener { process, content ->
            // nothing
        }
    }
}
