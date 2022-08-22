package com.qxdzbc.p6.message.api.connection.service.heart_beat.errors

import com.qxdzbc.p6.common.exception.error.ErrorHeader

object HBServiceErrors {
    private const val prefix = "HBErr "
    object CantStartHBService {
        val header= ErrorHeader("${prefix}1", "Can't start heart beat service")
        class Data (val additionalInfo:String)
    }

    object HBIsDead {
        val header=ErrorHeader("${prefix}2", "Heart beat service is dead")
        class Data (val additionalInfo:String)
    }

    object HBCrash {
        val header=ErrorHeader("${prefix}3", "Heart beat service crashed")
        class Data (val additionalInfo:String)
    }
}
