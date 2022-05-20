package com.emeraldblast.p6.message.api.message.protocol.errors

import com.emeraldblast.p6.common.exception.error.ErrorHeader
import com.emeraldblast.p6.message.api.message.protocol.ProtocolConstant
import java.io.IOException

object MsgProtocolErrors {
    private const val prefix = "Msg protocol error "

    object IOError {
        val header = ErrorHeader("$prefix 1", "io exception report")
        class Data(val exception: IOException)
    }

    object InvalidPayloadSizeError {
        val header = ErrorHeader("$prefix 2", "invalid payload size")

        class Data(val actualSize: Int, val correctSize: Int)
    }

    object DelimiterNotFound {
        val header = ErrorHeader(
            "$prefix 3",
            "can't find ZMQ special delimiter ${ProtocolConstant.messageDelimiter} in the raw message"
        )

        class Data(val payload: List<ByteArray>)
    }
}
