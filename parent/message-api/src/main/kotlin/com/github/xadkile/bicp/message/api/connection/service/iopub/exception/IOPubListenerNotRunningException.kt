package com.github.xadkile.bicp.message.api.connection.service.iopub.exception

import com.github.xadkile.bicp.message.api.connection.kernel_context.exception.KernelIsDownException

class IOPubListenerNotRunningException(msg:String) : Exception(msg) {
    companion object {
        fun occurAt(o:Any): IOPubListenerNotRunningException {
            return IOPubListenerNotRunningException("occur at ${o.javaClass.simpleName}")
        }
    }
}
