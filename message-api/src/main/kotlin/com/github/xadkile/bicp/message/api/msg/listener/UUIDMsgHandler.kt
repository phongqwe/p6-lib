package com.github.xadkile.bicp.message.api.msg.listener

import com.github.xadkile.bicp.message.api.protocol.message.JPRawMessage
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
//    companion object {
//        fun make(handlerFunction:(msg: JPRawMessage)->Unit): UUIDMsgHandler {
//            return object: UUIDMsgHandler(){
//                override fun handle(msg: JPRawMessage) {
//                    handlerFunction(msg)
//                }
//            }
//        }
//    }
}
