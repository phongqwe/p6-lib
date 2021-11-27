package com.github.xadkile.bicp.message.api.msg.listener

import com.github.xadkile.bicp.message.api.msg.protocol.message.JPRawMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgType

interface MsgHandler {
    /**
     * callback function
     * A handler may need to interact with other suspend function, so this function is a suspend function
     */
    suspend fun handle(msg: JPRawMessage)

    /**
     * unique id
     */
    fun id(): String
    fun msgType():MsgType
}

