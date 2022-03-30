package com.github.xadkile.p6.message.api.message.protocol.errors

import com.github.xadkile.p6.common.exception.error.ErrorHeader
import com.github.xadkile.p6.message.api.message.protocol.ProtocolConstant
import java.io.IOException

object MsgProtocolErrors {
    private const val prefix = "Msg protocol error "
    object IOError: ErrorHeader("$prefix 1","io exception report"){
        class Data(val exception:IOException)
    }

    object InvalidPayloadSizeError: ErrorHeader("$prefix 2","invalid payload size"){
        class Data(val actualSize:Int, val correctSize:Int)
    }

    object DelimiterNotFound: ErrorHeader("$prefix 3","can't find ZMQ special delimiter ${ProtocolConstant.messageDelimiter} in the raw message"){
        class Data(val payload:List<ByteArray>)
    }
}
