package com.github.xadkile.bicp.message.api.connection.kernel_context.exception

import com.github.xadkile.bicp.message.api.exception.ExceptionInfo

class CantStartProcess(val exceptionInfo:ExceptionInfo<String>):Exception(exceptionInfo.toString()) {
}
