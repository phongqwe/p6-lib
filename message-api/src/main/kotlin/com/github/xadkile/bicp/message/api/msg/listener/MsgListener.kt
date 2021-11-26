package com.github.xadkile.bicp.message.api.msg.listener

import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgType

/**
 * Listen for in-coming message.
 * Dispatch message to the appropriate handlers.
 */
interface MsgListener : MsgHandlerContainer,AutoCloseable {
    fun start()
    fun stop()
    fun isRunning():Boolean
}

