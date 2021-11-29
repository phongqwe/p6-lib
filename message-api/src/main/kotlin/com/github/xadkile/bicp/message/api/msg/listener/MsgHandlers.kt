package com.github.xadkile.bicp.message.api.msg.listener

import com.github.xadkile.bicp.message.api.msg.protocol.message.JPRawMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgType

/**
 *
 */
class MsgHandlers {
    companion object {
        fun withUUID(msgType: MsgType,
                     handlerFunction:suspend  (msg: JPRawMessage, listener:MsgListener) -> Unit = {_,_->},
        ): UUIDMsgHandler {
            return object : UUIDMsgHandler() {
                private val mt = msgType
                override suspend  fun handle(msg: JPRawMessage, listener:MsgListener) {
                    handlerFunction(msg,listener)
                }

                override fun msgType(): MsgType {
                    return mt
                }
            }
        }
    }
}
