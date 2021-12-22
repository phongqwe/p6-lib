package com.github.xadkile.p6.message.api.connection.service.iopub.exception

import com.github.xadkile.p6.exception.ExceptionInfo

class IOPubListenerNotRunningException(val exceptionInfo: ExceptionInfo<Unit>) : Exception(exceptionInfo.toString()){

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
