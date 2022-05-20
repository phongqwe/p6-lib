package com.emeraldblast.p6.message.api.connection.service.errors

import com.emeraldblast.p6.common.exception.error.ErrorHeader

object ServiceErrors {
    private const val prefix = "Service Error "

    object ServiceNull {
        val header = ErrorHeader("$prefix 1", "Service is null")
        class Data(val serviceName: String)
    }
}
