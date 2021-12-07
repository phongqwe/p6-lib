package com.github.xadkile.bicp.message.api.connection.service.exception

import com.github.xadkile.bicp.message.api.connection.service.iopub.exception.IOPubListenerNotRunningException
import com.github.xadkile.bicp.message.api.exception.ExceptionInfo

class ServiceNullException(val exceptionInfo: ExceptionInfo<String>) : Exception(exceptionInfo.toString()){

    companion object {
        fun occurAt(o:Any, serviceName:String): ServiceNullException {
            return ServiceNullException(ExceptionInfo(
                msg = "Service is null",
                loc = o,
                data = serviceName
            ))
        }
    }
}
