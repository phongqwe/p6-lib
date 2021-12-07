package com.github.xadkile.bicp.message.api.exception

abstract class ExceptionWithInfo(val exceptionInfo: ExceptionInfo) : Exception() {
    override fun toString(): String {
        return exceptionInfo.toString()
    }

    constructor(
        message: String = "",
        loc: Class<*>? = null,
        data: Any? = null,
        dataStrMaker: () -> String = { data?.toString() ?: "" },
    ) : this(ExceptionInfo(message, loc, data, dataStrMaker))

    constructor(
        message: String = "",
        loc: String = "",
        data: Any? = null,
        dataStrMaker: () -> String = { data?.toString() ?: "" },
    ) : this(ExceptionInfo(message, loc, data, dataStrMaker))
}
