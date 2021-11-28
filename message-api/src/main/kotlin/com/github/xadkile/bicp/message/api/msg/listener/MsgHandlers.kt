package com.github.xadkile.bicp.message.api.msg.listener

import com.github.xadkile.bicp.message.api.msg.protocol.message.JPRawMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgType

class MsgHandlers {
    companion object {
        fun withUUID(msgType: MsgType,
                     handlerFunction:suspend  (msg: JPRawMessage, listener:MsgListener) -> Unit,
                     exceptionFunction: suspend (exception:Exception, listener:MsgListener)->Unit
        ): UUIDMsgHandler {
            return object : UUIDMsgHandler() {
                private val mt = msgType
                override suspend  fun handle(msg: JPRawMessage, listener:MsgListener) {
                    handlerFunction(msg,listener)
                }

                override suspend fun onListenerException(exception: Exception, listener: MsgListener) {
                    exceptionFunction(exception, listener)
                }

                override fun msgType(): MsgType {
                    return mt
                }
            }
        }
    }
}
