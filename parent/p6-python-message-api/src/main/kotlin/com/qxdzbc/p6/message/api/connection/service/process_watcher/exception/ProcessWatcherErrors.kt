package com.qxdzbc.p6.message.api.connection.service.process_watcher.exception

import com.qxdzbc.common.error.ErrorHeader

object ProcessWatcherErrors {
    private const val prefix = "Process Watcher error "
    object IllegalState {
        val header= ErrorHeader("${prefix}1","process watcher is in an illegal state")
        class Data(val currentState:String, val correctState:String)
    }

    object DeadProcess  {
        val header= ErrorHeader("${prefix}2","process is dead")
    }
}
