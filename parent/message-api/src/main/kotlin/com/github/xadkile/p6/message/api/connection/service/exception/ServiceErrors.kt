package com.github.xadkile.p6.message.api.connection.service.exception

import com.github.xadkile.p6.exception.lib.error.ErrorHeader

object ServiceErrors {
    private const val prefix = "Service Error "

    object ServiceNull : ErrorHeader("${prefix}1", "Service is null") {
        class Data(val serviceName: String)
    }
}
