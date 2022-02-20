package com.github.xadkile.p6.message.api.connection.service.heart_beat.errors

import com.github.xadkile.p6.common.exception.lib.error.ErrorType

object HBServiceErrors {
    private const val prefix = "Heart Beat Service error "
    object CantStartHBService : ErrorType("${prefix}1", "Can't start heart beat service"){
        class Data (val additionalInfo:String)
    }

    object HBIsDead : ErrorType("${prefix}2", "Heart beat service is dead"){
        class Data (val additionalInfo:String)
    }

    object HBCrash : ErrorType("${prefix}3", "Heart beat service crashed"){
        class Data (val additionalInfo:String)
    }
}
