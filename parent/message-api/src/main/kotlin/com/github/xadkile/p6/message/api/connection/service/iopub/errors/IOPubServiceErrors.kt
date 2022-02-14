package com.github.xadkile.p6.message.api.connection.service.iopub.errors

import com.github.xadkile.p6.exception.lib.error.ErrorType

object IOPubServiceErrors {
    private const val prefix = "IOPub service error "
    object CantStartIOPubServiceTimeOut : ErrorType("${prefix}1", "Can't start IO Pub service because of timeout"){
        class Data (val additionalInfo:String)
    }

    object IOPubServiceNotRunning : ErrorType("${prefix}2", "IO Pub service is not running"){
        class Data (val additionalInfo:String)
    }
}
