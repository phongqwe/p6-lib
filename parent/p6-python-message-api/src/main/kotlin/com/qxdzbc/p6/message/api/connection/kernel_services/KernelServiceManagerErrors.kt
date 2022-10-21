package com.qxdzbc.p6.message.api.connection.kernel_services

import com.qxdzbc.common.error.ErrorHeader
import com.qxdzbc.common.error.ErrorReport

object KernelServiceManagerErrors {
    val prefix = "KernelServiceManagerErrors_"
    object CantStartServices{
        val header = ErrorHeader("${prefix}0","Can't start kernel services")
        fun report(detail:String): ErrorReport {
            return header.setDescription(detail).toErrorReport()
        }
    }
}
