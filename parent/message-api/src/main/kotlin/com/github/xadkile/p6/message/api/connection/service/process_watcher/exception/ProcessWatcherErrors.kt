package com.github.xadkile.p6.message.api.connection.service.process_watcher.exception

import com.github.xadkile.p6.common.exception.error.ErrorHeader

object ProcessWatcherErrors {
    private const val prefix = "Process Watcher error "
    object IllegalState : ErrorHeader("${prefix}1","process watcher is in an illegal state"){
        class Data(val currentState:String, val correctState:String)
    }

    object DeadProcess : ErrorHeader("${prefix}2","process is dead"){
    }
}
