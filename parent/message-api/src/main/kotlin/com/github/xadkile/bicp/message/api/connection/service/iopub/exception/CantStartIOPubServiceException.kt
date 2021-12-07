package com.github.xadkile.bicp.message.api.connection.service.iopub.exception

import com.github.xadkile.bicp.message.api.exception.ExceptionInfo

class CantStartIOPubServiceException (val exceptionInfo: ExceptionInfo<Any> ):Exception(exceptionInfo.toString()) {
}

