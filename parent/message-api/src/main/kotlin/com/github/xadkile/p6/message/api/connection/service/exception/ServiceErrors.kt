package com.github.xadkile.p6.message.api.connection.service.exception

import com.github.xadkile.p6.exception.error.ErrorHeader

object ServiceErrors {
    object ServiceNull : ErrorHeader("ServiceNull".hashCode(), "Service is null"){
        class Data (val serviceName:String)
    }
}
