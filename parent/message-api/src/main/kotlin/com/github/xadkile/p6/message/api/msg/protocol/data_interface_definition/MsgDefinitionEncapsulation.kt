package com.github.xadkile.p6.message.api.msg.protocol.data_interface_definition

import com.github.xadkile.p6.message.api.connection.service.iopub.MsgHandler
import com.github.xadkile.p6.message.api.connection.service.iopub.MsgHandlers
import com.github.xadkile.p6.message.api.msg.protocol.JPRawMessage
import com.github.xadkile.p6.message.api.msg.protocol.MsgType

interface MsgDefinitionEncapsulation {
    val msgType:MsgType
}

fun MsgDefinitionEncapsulation.handler(
    handlerFunction:  (msg: JPRawMessage) -> Unit)
: MsgHandler {
    return MsgHandlers.withUUID(msgType, handlerFunction)
}
