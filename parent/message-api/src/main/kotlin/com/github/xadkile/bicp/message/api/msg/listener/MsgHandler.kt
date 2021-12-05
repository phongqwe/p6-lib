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
     * This function may be long running, and I want to run it inside a coroutine ??????
     */
    fun handle(msg: JPRawMessage)

    /**
     * unique id
     */
    fun id(): String
    fun msgType(): MsgType
}

