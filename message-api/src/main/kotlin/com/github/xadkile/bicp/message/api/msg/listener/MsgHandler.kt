package com.github.xadkile.bicp.message.api.msg.listener

import com.github.xadkile.bicp.message.api.msg.protocol.message.JPRawMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgType

interface MsgHandler {
    /**
     * callback function
     */
    fun handle(msg: JPRawMessage)

    /**
     * unique id
     */
    fun id(): String
    fun msgType():MsgType
}

