package com.github.xadkile.p6.exception

import com.github.xadkile.p6.exception.ExceptionInfo

class CompositeException(val exceptionInfo: ExceptionInfo<List<Exception>>) : Exception(exceptionInfo.toString()) {
    constructor(exceptions: List<Exception>) : this(ExceptionInfo(
        msg = "",
        loc = "",
        data = exceptions,
        dataStrMaker = {
            exceptions.joinToString("\n") { it.toString() }
        }
    ))
}
