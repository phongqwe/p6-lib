package com.github.xadkile.p6.common.exception.error

object CommonErrors {
    private const val prefix = "Common Error "

    /**
     */
    object TimeOut : ErrorHeader("${prefix}1","Timeout"){
        data class Data(val detail:String)
    }

    /**
     * For reporting unknown exception
     */
    object Unknown : ErrorHeader("${prefix}2","Unknown error"){
        data class Data(val additionalInfo:String, val exception:Exception?)
    }

    /**
     * this error indicates that an exception was caught
     */
    object ExceptionError : ErrorHeader("${prefix}3","Exception error"){
        data class Data(val exception:Exception)
    }

    object MultipleErrors: ErrorHeader("$prefix 3","Multiple errors"){
        data class Data(val errorList:List<ErrorReport>)
    }
}
