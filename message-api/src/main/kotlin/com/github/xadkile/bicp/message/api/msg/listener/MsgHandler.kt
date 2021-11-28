package com.github.xadkile.bicp.message.api.msg.listener

import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.msg.protocol.message.JPRawMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgType

/**
 * For use with [MsgListener].
 * Hold code that reacts on certain occasion
 */
interface MsgHandler {
    /**
     * callback function
     * A handler may need to interact with other suspend function, so this function is a suspend function
     */
    suspend fun handle(msg: JPRawMessage, listener: MsgListener)

    /**
     * A callback function which can be use to notify handler of exception (of my choice) that happened in [MsgListener]
     */
    suspend fun onListenerException(exception: Exception, listener: MsgListener)

    /**
     * unique id
     */
    fun id(): String
    fun msgType(): MsgType
}

