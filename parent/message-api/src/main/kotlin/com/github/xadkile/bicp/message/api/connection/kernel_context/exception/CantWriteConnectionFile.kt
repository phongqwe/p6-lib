package com.github.xadkile.bicp.message.api.connection.kernel_context.exception

import com.github.xadkile.bicp.exception.ExceptionInfo

class CantWriteConnectionFile(val exceptionInfo: ExceptionInfo<String>):Exception(exceptionInfo.toString()) {
}
