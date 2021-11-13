package com.github.xadkile.bicp.message.api.connection

import com.github.xadkile.bicp.message.api.protocol.message.JPMessage

interface MsgEncoder {
    fun encodeMessage(message: JPMessage<*, *>): List<ByteArray>
}

