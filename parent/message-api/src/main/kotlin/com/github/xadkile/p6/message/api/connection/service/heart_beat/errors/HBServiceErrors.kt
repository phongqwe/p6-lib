package com.github.xadkile.p6.message.api.connection.service.heart_beat.errors

import com.github.xadkile.p6.exception.error.ErrorHeader

object HBServiceErrors {
    object CantStartHBService : ErrorHeader("CantStartHBService".hashCode(), "Can't start heart beat service"){
        class Data (val additionalInfo:String)
    }

    object HBIsDead : ErrorHeader("HBIsDead".hashCode(), "Heart beat service is dead"){
        class Data (val additionalInfo:String)
    }

    object HBCrash : ErrorHeader("HBCrash".hashCode(), "Heart beat service crashed"){
        class Data (val additionalInfo:String)
    }
}
