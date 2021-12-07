package com.github.xadkile.bicp.message.api.connection.kernel_context.exception

import com.github.xadkile.bicp.message.api.exception.ExceptionInfo

class CantStopKernelProcess(val exceptionInfo: ExceptionInfo<Long?>):Exception(exceptionInfo.toString()) {
}
