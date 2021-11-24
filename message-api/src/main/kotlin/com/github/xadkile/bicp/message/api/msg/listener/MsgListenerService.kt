package com.github.xadkile.bicp.message.api.msg.listener

import com.github.xadkile.bicp.message.api.protocol.message.MsgType

/**
 * Listen for in-coming message.
 * Dispatch message to the appropriate handlers.
 */
interface MsgListenerService : MsgHandlerContainer {
    fun start()
    fun stop()
    fun isRunning():Boolean
}

