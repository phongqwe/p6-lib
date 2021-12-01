package com.github.xadkile.bicp.message.api.msg.listener

import com.github.xadkile.bicp.message.api.msg.protocol.JPRawMessage
import com.github.xadkile.bicp.message.api.msg.protocol.MsgType

/**
 * For use with [MsgListener].
 * Hold code that reacts on certain occasion
 */
interface MsgHandler {
    /**
     * callback function.
     * A handler may need to interact with other suspend function, so this function is a suspend function.
     */
    suspend fun handle(msg: JPRawMessage, listener: MsgListener)

    /**
     * unique id
     */
    fun id(): String
    fun msgType(): MsgType
}

