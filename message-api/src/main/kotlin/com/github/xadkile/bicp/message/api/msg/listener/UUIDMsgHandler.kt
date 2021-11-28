package com.github.xadkile.bicp.message.api.msg.listener

import com.github.xadkile.bicp.message.api.msg.protocol.message.JPRawMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgType
import java.util.*

/**
 * A handler whose id is a random uuid
 */
abstract class UUIDMsgHandler : MsgHandler {
    private val _id:String by lazy {
        UUID.randomUUID().toString()
    }
    override fun id(): String {
        return _id
    }
    companion object {
        fun make(msgType: MsgType, handlerFunction:suspend  (msg: JPRawMessage, listener:MsgListener) -> Unit ): UUIDMsgHandler {
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
