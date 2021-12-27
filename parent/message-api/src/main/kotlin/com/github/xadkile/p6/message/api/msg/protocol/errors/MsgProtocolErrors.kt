package com.github.xadkile.p6.message.api.msg.protocol.errors

import com.github.xadkile.p6.exception.error.ErrorHeader
import com.github.xadkile.p6.message.api.msg.protocol.ProtocolConstant
import java.io.IOException

object MsgProtocolErrors {
    object IOError: ErrorHeader("MsgProtocolErrors.IOError".hashCode(),"io exception report"){
        class Data(val exception:IOException)
    }

    object InvalidPayloadSizeError: ErrorHeader("MsgProtocolErrors.InvalidPayloadSizeError".hashCode(),"invalid payload size"){
        class Data(val actualSize:Int, val correctSize:Int)
    }

    object DelimiterNotFound: ErrorHeader("MsgProtocolErrors.DelimiterNotFound".hashCode(),"can't find ZMQ special delimiter ${ProtocolConstant.messageDelimiter} in the raw message"){
        class Data(val payload:List<ByteArray>)
    }
}
