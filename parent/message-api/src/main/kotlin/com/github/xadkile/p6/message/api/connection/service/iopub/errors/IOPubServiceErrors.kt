package com.github.xadkile.p6.message.api.connection.service.iopub.errors

import com.github.xadkile.p6.common.exception.error.ErrorHeader

object IOPubServiceErrors {
    private const val prefix = "IOPub service error "
    object CantStartIOPubServiceTimeOut : ErrorHeader("${prefix}1", "Can't start IO Pub service because of timeout"){
        class Data (val additionalInfo:String)
    }

    object IOPubServiceNotRunning : ErrorHeader("${prefix}2", "IO Pub service is not running"){
        class Data (val additionalInfo:String)
    }
}
