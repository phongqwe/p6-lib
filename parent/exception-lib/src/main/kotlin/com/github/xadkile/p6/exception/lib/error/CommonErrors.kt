package com.github.xadkile.p6.exception.lib.error

object CommonErrors {
    private const val prefix = "Common Error "

    /**
     */
    object TimeOut : ErrorType("${prefix}1","Timeout"){
        data class Data(val detail:String)
    }

    /**
     * For reporting unknown exception
     */
    object Unknown : ErrorType("${prefix}2","Unknown error"){
        data class Data(val additionalInfo:String, val exception:Exception?)
    }

    /**
     * this error indicates that an exception was caught
     */
    object ExceptionError : ErrorType("${prefix}3","Exception error"){
        data class Data(val exception:Exception)
    }
}
