package com.emeraldblast.p6.message.api.connection.service.process_watcher

fun interface OnStdErrEventProcessListener {
    fun onStdErr(process: Process, content: String)

    companion object {
        val nothing = OnStdErrEventProcessListener { process, content ->
            // nothing
        }
    }
}
