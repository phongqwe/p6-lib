package com.github.xadkile.p6.message.api.connection.kernel_context.exception

import com.github.xadkile.p6.exception.ExceptionInfo

class CantWriteConnectionFile(val exceptionInfo: ExceptionInfo<String>):Exception(exceptionInfo.toString()) {
}
