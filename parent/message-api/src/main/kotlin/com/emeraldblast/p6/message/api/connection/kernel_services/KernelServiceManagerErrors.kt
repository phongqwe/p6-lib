package com.emeraldblast.p6.message.api.connection.kernel_services

import com.emeraldblast.p6.common.exception.error.ErrorHeader
import com.emeraldblast.p6.common.exception.error.ErrorReport

object KernelServiceManagerErrors {
    val prefix = "KernelServiceManagerErrors_"
    object CantStartServices{
        val header = ErrorHeader("${prefix}0","Can't start kernel services")
        fun report(detail:String):ErrorReport{
            return header.setDescription(detail).toErrorReport()
        }
    }
}
