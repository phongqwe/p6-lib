package com.emeraldblast.p6.message.api.connection.service.iopub

import com.emeraldblast.p6.message.api.message.protocol.JPRawMessage
import com.emeraldblast.p6.message.api.message.protocol.MsgType

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
