package com.github.xadkile.p6.exception.error

object CommonErrors {
    object TimeOut : ErrorHeader("TIME_OUT".hashCode(),"Timeout"){
        data class Data(val detail:String)
    }

    /**
     * For reporting unknown exception
     */
    object Unknown : ErrorHeader("UNKNOWN".hashCode(),"Unknown error"){
        data class Data(val additionalInfo:String, val exception:Exception?)
    }

    object ExceptError : ErrorHeader("ExceptError".hashCode(),"Exception error"){
        data class Data(val exception:Exception)
    }
}
