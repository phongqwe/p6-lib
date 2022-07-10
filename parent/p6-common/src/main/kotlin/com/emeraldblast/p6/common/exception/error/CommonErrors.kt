package com.emeraldblast.p6.common.exception.error

object CommonErrors {
    private const val prefix = "Common Error "

    /**
     */
    object TimeOut {
        val header = ErrorHeader("${prefix}1", "Timeout")
        data class Data(val detail: String)
        fun report(detail:String):ErrorReport{
            return header.setDescription(detail).toErrorReport()
        }
    }

    /**
     * For reporting unknown exception
     */
    object Unknown {
        val header = ErrorHeader("${prefix}2", "Unknown error")

        data class Data(val additionalInfo: String, val exception: Exception?)
        fun report(detail: String):ErrorReport{
            return header.setDescription(detail).toErrorReport()
        }
    }

    /**
     * this error indicates that an exception was caught
     */
    object ExceptionError {
        val header = ErrorHeader("${prefix}3", "Exception error")

        data class Data(val exception: Exception)

        fun report(exception: Exception): ErrorReport {
            return ErrorReport(
                header = header.appendDescription(":${exception}"),
                data = Data(exception)
            )
        }

        /**
         * try-catch some function, if an exception is caught, the exception is wrapped inside an error report and return, otherwise, return null
         */
        fun tryDo(f:()->Unit):ErrorReport?{
            try{
                f()
                return null
            }catch (e:Exception){
                return report(e)
            }
        }
    }

    object MultipleErrors {
        val header = ErrorHeader("$prefix 3", "Multiple errors")

        data class Data(val errorList: List<ErrorReport>)

        fun report(errorList: List<ErrorReport>):ErrorReport{
            return ErrorReport(
                header = header,
                data =Data(errorList)
            )
        }
    }
}
