package com.github.xadkile.p6.message.api.connection.service.process_watcher.exception

import com.github.xadkile.p6.exception.error.ErrorHeader
import com.github.xadkile.p6.exception.error.ErrorReport

object ProcessWatcherErrors {
    object IllegalState : ErrorHeader("ProcessWatcherErrors.IllegalState".hashCode(),"process watcher is in an illegal state"){
        class Data(val currentState:String, val correctState:String)
    }

    object DeadProcess : ErrorHeader("ProcessWatcherErrors.DeadProcess".hashCode(),"process is dead"){
    }
}
