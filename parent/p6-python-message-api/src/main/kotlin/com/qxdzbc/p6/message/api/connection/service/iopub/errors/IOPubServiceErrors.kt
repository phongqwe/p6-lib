package com.qxdzbc.p6.message.api.connection.service.iopub.errors

import com.qxdzbc.common.error.ErrorHeader
import com.qxdzbc.common.error.ErrorReport

object IOPubServiceErrors {
    private const val prefix = "IOPub service error "
    object CantStartIOPubServiceTimeOut {
        val header= ErrorHeader("${prefix}1", "Can't start IO Pub service because of timeout")
        class Data (val additionalInfo:String)
    }

    object IOPubServiceNotRunning {
        val header= ErrorHeader("${prefix}2", "IO Pub service is not running")
        class Data (val additionalInfo:String)
        fun report(detail:String): ErrorReport {
            return header.setDescription(detail).toErrorReport()
        }
    }
}
