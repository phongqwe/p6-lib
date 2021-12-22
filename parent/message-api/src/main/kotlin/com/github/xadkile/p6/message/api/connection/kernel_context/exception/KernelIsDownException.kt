package com.github.xadkile.p6.message.api.connection.kernel_context.exception

import com.github.xadkile.p6.exception.ExceptionInfo

class KernelIsDownException(val exceptionInfo: ExceptionInfo<Any>) : Exception(exceptionInfo.toString()) {

    companion object {
        fun occurAt(o: Any): KernelIsDownException {
            return KernelIsDownException(
                ExceptionInfo(
                    msg = "Kernel is down",
                    loc = o,
                    data = Unit
                )
            )
        }
    }
}
