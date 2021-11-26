package com.github.xadkile.bicp.message.api.msg.listener

import com.github.michaelbull.result.Result
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
        val default = withUUID(MsgType.DONT_EXIST){
            println(it)
        }
    }
}
