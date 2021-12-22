package com.github.xadkile.p6.message.api.connection.service.iopub

import com.github.xadkile.p6.message.api.msg.protocol.JPRawMessage
import com.github.xadkile.p6.message.api.msg.protocol.MsgType

/**
 *
 */
class MsgHandlers {
    companion object {
        fun withUUID(msgType: MsgType,
                     handlerFunction: (msg: JPRawMessage) -> Unit = {},
        ): UUIDMsgHandler {
            return object : UUIDMsgHandler() {
                private val mt = msgType
                override   fun handle(msg: JPRawMessage) {
                    handlerFunction(msg)
                }

                override fun msgType(): MsgType {
                    return mt
                }
            }
        }
    }
}
