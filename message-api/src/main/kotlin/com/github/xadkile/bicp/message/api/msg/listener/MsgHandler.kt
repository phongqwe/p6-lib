package com.github.xadkile.bicp.message.api.msg.listener

import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.msg.protocol.message.JPRawMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgType

interface MsgHandler {
    /**
     * callback function
     */
    suspend fun handle(msg: JPRawMessage)

    /**
     * unique id
     */
    fun id(): String
    fun msgType():MsgType
}

