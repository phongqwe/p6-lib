package com.github.xadkile.p6.message.api.connection.kernel_context.exception

import com.github.xadkile.p6.exception.ExceptionInfo

class KernelServiceDownException(val exceptionInfo: ExceptionInfo<Any>) : Exception(exceptionInfo.toString()) {

    companion object {
        fun occurAt(o: Any): KernelServiceDownException {
            return KernelServiceDownException(
                ExceptionInfo(
                    msg = "Kernel services are down",
                    loc = o,
                    data = Unit
                )
            )
        }
    }
}
