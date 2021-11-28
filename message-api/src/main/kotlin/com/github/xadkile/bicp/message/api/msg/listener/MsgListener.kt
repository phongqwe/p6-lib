package com.github.xadkile.bicp.message.api.msg.listener

import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.connection.util.HaveKernelContext
import com.github.xadkile.bicp.message.api.msg.protocol.message.JPRawMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.lang.Exception

/**
 * Listen for in-coming message.
 * Dispatch message to the appropriate handlers.
 *
 */
interface MsgListener : MsgHandlerContainer, AutoCloseable, HaveKernelContext {

    /**
     * A listener may outlive the scope in which it is launch, so inject a scope in the start function
     */
    suspend fun start(
        externalScope: CoroutineScope,
        cDispatcher: CoroutineDispatcher = Dispatchers.IO,
    ): Result<Unit, Exception>

    suspend fun stop()

    fun isRunning(): Boolean
}

