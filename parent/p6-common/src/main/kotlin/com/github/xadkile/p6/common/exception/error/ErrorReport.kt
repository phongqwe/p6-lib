package com.github.xadkile.p6.common.exception.error

class ErrorReport(
    val header: ErrorHeader,
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
        return this.header.toString()
    }
    fun isType(errorHeader: ErrorHeader):Boolean{
        return this.header.isType(errorHeader)
    }
}


