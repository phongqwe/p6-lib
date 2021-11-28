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
}
