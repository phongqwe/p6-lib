package com.github.xadkile.p6.message.api.connection.kernel_context.exception

import com.github.xadkile.p6.exception.ExceptionInfo

class CantStopKernelProcess(val exceptionInfo: ExceptionInfo<Long?>):Exception(exceptionInfo.toString()) {
}
