package com.github.xadkile.bicp.message.api.msg.listener

import com.github.xadkile.bicp.message.api.msg.protocol.message.JPRawMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgType

class MsgHandlers {
    companion object {
        fun withUUID(msgType: MsgType, handlerFunction: (msg: JPRawMessage) -> Unit = {}): UUIDMsgHandler {
            return object : UUIDMsgHandler() {
                private val mt = msgType
                override fun handle(msg: JPRawMessage) {
                    handlerFunction(msg)
                }
                override fun msgType(): MsgType {
                    return mt
                }
            }
        }
    }
}
