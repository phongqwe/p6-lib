package com.github.xadkile.p6.message.api.connection.service.iopub.exception

import com.github.xadkile.p6.exception.error.ErrorHeader
import com.github.xadkile.p6.message.api.msg.protocol.MsgContent

object IOPubServiceErrors {

    object CantStartIOPubServiceTimeOut : ErrorHeader("CantStartIOPubService".hashCode(), "Can't start IO Pub service"){
        class Data (val additionalInfo:String)
    }

    object ExecutionError : ErrorHeader("ExecutionError".hashCode(), "Execution error"){
        class Data (val messageContent:MsgContent)
    }

    object ExecutionAbort : ErrorHeader("ExecutionAbort".hashCode(), "Execution aborted"){
        class Data (val messageContent:MsgContent)
    }

    object IOPubServiceNotRunning : ErrorHeader("IOPubServiceNotRunning".hashCode(), "IO Pub service is not running"){
        class Data (val additionalInfo:String)
    }
}
