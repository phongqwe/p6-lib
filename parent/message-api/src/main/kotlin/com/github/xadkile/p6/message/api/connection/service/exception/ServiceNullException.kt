package com.github.xadkile.p6.message.api.connection.service.exception

import com.github.xadkile.p6.exception.lib.ExceptionInfo

class ServiceNullException(val exceptionInfo: ExceptionInfo<String>) : Exception(exceptionInfo.toString()){

    companion object {
        fun occurAt(o:Any, serviceName:String): ServiceNullException {
            return ServiceNullException(
                ExceptionInfo(
                msg = "Service is null",
                loc = o,
                data = serviceName
            )
            )
        }
    }
}
