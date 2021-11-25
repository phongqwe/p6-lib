package com.github.xadkile.bicp.message.api.connection.process_watcher

fun interface OnStdErrEventProcessListener {
    fun onStdErr(process: Process, content: String)

    companion object {
        val nothing = OnStdErrEventProcessListener { process, content ->
            // nothing
        }
    }
}
