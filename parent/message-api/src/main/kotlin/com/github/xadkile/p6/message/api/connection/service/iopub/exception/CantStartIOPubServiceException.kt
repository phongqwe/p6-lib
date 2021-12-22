package com.github.xadkile.p6.message.api.connection.service.iopub.exception

import com.github.xadkile.p6.exception.ExceptionInfo

class CantStartIOPubServiceException (val exceptionInfo: ExceptionInfo<Any>):Exception(exceptionInfo.toString()) {
}

