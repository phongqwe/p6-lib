package com.github.xadkile.p6.message.api.connection.service.iopub

import com.github.xadkile.p6.message.api.msg.protocol.JPRawMessage
import com.github.xadkile.p6.message.api.msg.protocol.MsgType

/**
 *
 */
object MsgHandlers {
    /**
     * create a [MsgHandler] with a random uuid
     */
    fun withUUID(
        msgType: MsgType,
        handlerFunction: (msg: JPRawMessage) -> Unit = {},
    ): UUIDMsgHandler {
        return object : UUIDMsgHandler() {
            private val mt = msgType
            override fun handle(msg: JPRawMessage) {
                handlerFunction(msg)
            }
            override val msgType: MsgType = mt
        }
    }
}
