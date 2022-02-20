package com.github.xadkile.p6.common.exception.lib.error

class ErrorReport(
    val type: ErrorType,
    val data: Any,
    val loc:String="",
) {
    fun <T> getCastedData(): T {
        return this.data as T
    }

    fun toException(): ErrorException {
        return ErrorException(this)
    }

    override fun toString(): String {
        return """
            ${type.toString()}
            data: ${this.data.toString()}
            loc: ${this.loc}
        """.trimIndent()
    }
    fun isType(errorType: ErrorType):Boolean{
        return this.type.isType(errorType)
    }
}


