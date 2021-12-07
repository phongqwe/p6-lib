package com.github.xadkile.bicp.message.api.connection.service.iopub.exception

import com.github.xadkile.bicp.message.api.exception.ExceptionInfo

class IOPubListenerNotRunningException(val exceptionInfo:ExceptionInfo<Unit>) : Exception(exceptionInfo.toString()){

    companion object {
        fun occurAt(o:Any): IOPubListenerNotRunningException {
            return IOPubListenerNotRunningException(ExceptionInfo(
                msg = "IOPubListener not running",
                loc = o,
                data = Unit
            ))
        }
    }
}
