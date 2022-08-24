package com.qxdzbc.p6.message.api.message.protocol.data_interface_definition

import com.qxdzbc.p6.message.api.connection.service.iopub.handler.MsgHandler
import com.qxdzbc.p6.message.api.connection.service.iopub.handler.MsgHandlers
import com.qxdzbc.p6.message.api.message.protocol.JPRawMessage
import com.qxdzbc.p6.message.api.message.protocol.MsgType

interface MsgDefinitionEncapsulation {
    val msgType:MsgType
    fun handler(
        handlerFunction:  (msg: JPRawMessage) -> Unit)
            : MsgHandler {
        return MsgHandlers.withUUID(msgType, handlerFunction)
    }
}

