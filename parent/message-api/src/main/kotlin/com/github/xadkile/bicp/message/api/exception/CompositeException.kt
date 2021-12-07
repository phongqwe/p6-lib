package com.github.xadkile.bicp.message.api.exception

class CompositeException(exceptionInfo: ExceptionInfo) : ExceptionWithInfo(exceptionInfo) {
    constructor(exceptions: List<Exception>) : this(ExceptionInfo("", "", exceptions, dataStrMaker = {
        exceptions.joinToString("\n") { it.toString() }
    }))
}
