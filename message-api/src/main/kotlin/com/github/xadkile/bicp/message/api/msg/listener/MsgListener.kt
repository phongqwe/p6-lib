package com.github.xadkile.bicp.message.api.msg.listener

import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * Listen for in-coming message.
 * Dispatch message to the appropriate handlers.
 *
 */
interface MsgListener : MsgHandlerContainer, AutoCloseable {

    /**
     * A listener may outlive the scope in which it is launch, so inject a scope in the start function
     */
    suspend fun start(
        externalScope: CoroutineScope,
        cDispatcher: CoroutineDispatcher = Dispatchers.Default)

    suspend fun stop()

    fun isRunning(): Boolean
}

