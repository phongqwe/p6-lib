package com.emeraldblast.p6.common.exception.error

class ErrorReport(
    val header: ErrorHeader,
    val data: Any? = null,
    val exception:Exception? = null,
) {
    /**
     * Convert this into an exception. If already hold an exception, return that exception
     */
    fun toException(): Exception {
        if(exception!=null){
            return exception
        }
        return this.toErrorException()
    }

    /**
     * convert this [ErrorReport] into an [ErrorException]
     */
    fun toErrorException(): ErrorException {
        return ErrorException(this)
    }

    override fun toString(): String {
        val rt = """
type: ${this.header}
data: ${data}
        """.trimIndent()
        // loc: ${loc}
        return rt
    }

    fun isType(errorHeader: ErrorHeader): Boolean {
        return this.header.isType(errorHeader)
    }

    fun isType(errorReport: ErrorReport): Boolean {
        return this.header.isType(errorReport.header)
    }

    fun stackTraceStr(): String {
        val s = this.toException().stackTraceToString()
        return s
    }
    fun identicalTo(another:ErrorReport):Boolean{
        val c1 = this.isType(another)
        return c1 && this.data == another.data && this.exception == another.exception
    }
}


