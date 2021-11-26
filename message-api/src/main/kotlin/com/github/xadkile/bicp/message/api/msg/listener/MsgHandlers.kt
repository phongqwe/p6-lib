package com.github.xadkile.bicp.message.api.msg.listener

import com.github.xadkile.bicp.message.api.msg.protocol.message.JPRawMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgType

class MsgHandlers {
    companion object {
        fun withUUID(msgType: MsgType, handlerFunction: suspend (msg: JPRawMessage) -> Unit = {}): UUIDMsgHandler {
            return object : UUIDMsgHandler() {
                private val mt = msgType
                override suspend fun handle(msg: JPRawMessage) {
                    handlerFunction(msg)
                }
                override fun msgType(): MsgType {
                    return mt
                }
            }
        }
        val DoNothing = withUUID(MsgType.NOT_RECOGNIZE){}
        fun default(handlerFunction:suspend (msg: JPRawMessage) -> Unit = {}):UUIDMsgHandler{
            return withUUID(MsgType.NOT_RECOGNIZE,handlerFunction)
        }
    }
}
