package com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition

import com.github.xadkile.bicp.message.api.msg.listener.MsgHandler
import com.github.xadkile.bicp.message.api.msg.listener.MsgHandlers
import com.github.xadkile.bicp.message.api.msg.listener.MsgListener
import com.github.xadkile.bicp.message.api.msg.protocol.message.JPRawMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgType

interface MsgDefinitionEncapsulation {
    fun getMsgType2(): MsgType
}

fun MsgDefinitionEncapsulation.handler(
    handlerFunction:suspend  (msg: JPRawMessage, listener: MsgListener) -> Unit)
: MsgHandler {
    return MsgHandlers.withUUID(getMsgType2(), handlerFunction)
}
