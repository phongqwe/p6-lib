package com.github.xadkile.p6.exception.error

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
}


