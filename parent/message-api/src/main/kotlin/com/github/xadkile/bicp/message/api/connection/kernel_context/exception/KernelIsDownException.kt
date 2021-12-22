package com.github.xadkile.bicp.message.api.connection.kernel_context.exception

import com.github.xadkile.bicp.exception.ExceptionInfo

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
