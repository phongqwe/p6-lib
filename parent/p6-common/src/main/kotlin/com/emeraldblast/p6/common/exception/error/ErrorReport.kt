package com.emeraldblast.p6.common.exception.error

class ErrorReport(
    val header: ErrorHeader,
    val data: Any? = null,
    val exception: Exception? = null,
) {
    /**
     * Convert this into an exception. If already hold an exception, return that exception
     */
    fun toException(): Exception {
        if (exception != null) {
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
${if (data != null) "data:${data}" else ""}
        """.trimIndent()
        // loc: ${loc}
        return rt
    }

    /**
     * @return true if this report is reporting the same error (same error code) as another error header, false otherwise
     */
    fun isType(errorHeader: ErrorHeader): Boolean {
        return this.header.isType(errorHeader)
    }

    /**
     * @return true if this report is reporting the same error (same error code) as another report, false otherwise
     */
    fun isType(errorReport: ErrorReport): Boolean {
        return this.header.isType(errorReport.header)
    }

    /**
     * produce stack trace as a string
     * @return stack trace as a string
     */
    fun stackTraceStr(): String {
        val s = this.toException().stackTraceToString()
        return s
    }

    /**
     * @return true if this error report is identical in every way to another report
     */
    fun identicalTo(another: ErrorReport): Boolean {
        val c1 = this.isType(another)
        return c1 && this.data == another.data && this.exception == another.exception
    }
}


