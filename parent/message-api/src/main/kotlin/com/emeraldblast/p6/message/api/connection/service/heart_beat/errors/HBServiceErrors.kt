package com.emeraldblast.p6.message.api.connection.service.heart_beat.errors

import com.emeraldblast.p6.common.exception.error.ErrorHeader

object HBServiceErrors {
    private const val prefix = "Heart Beat Service error "
    object CantStartHBService : ErrorHeader("${prefix}1", "Can't start heart beat service"){
        class Data (val additionalInfo:String)
    }

    object HBIsDead : ErrorHeader("${prefix}2", "Heart beat service is dead"){
        class Data (val additionalInfo:String)
    }

    object HBCrash : ErrorHeader("${prefix}3", "Heart beat service crashed"){
        class Data (val additionalInfo:String)
    }
}
