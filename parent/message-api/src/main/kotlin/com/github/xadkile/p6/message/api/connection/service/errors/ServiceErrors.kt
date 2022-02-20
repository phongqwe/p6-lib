package com.github.xadkile.p6.message.api.connection.service.errors

import com.github.xadkile.p6.common.exception.lib.error.ErrorType

object ServiceErrors {
    private const val prefix = "Service Error "

    object ServiceNull : ErrorType("$prefix 1", "Service is null") {
        class Data(val serviceName: String)
    }
}
