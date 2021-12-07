package com.github.xadkile.bicp.message.api.connection.service.iopub.exception

import com.github.xadkile.bicp.message.api.connection.kernel_context.exception.KernelIsDownException
import com.github.xadkile.bicp.message.api.exception.ExceptionInfo
import com.github.xadkile.bicp.message.api.exception.ExceptionWithInfo

class IOPubListenerNotRunningException(exceptionInfo:ExceptionInfo) : ExceptionWithInfo(exceptionInfo) {

    companion object {
        fun occurAt(o:Any): IOPubListenerNotRunningException {
            return ExceptionInfo.occurAt(o).toException()
        }
    }
}
