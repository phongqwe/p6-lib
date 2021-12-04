package com.github.xadkile.bicp.message.api.msg.listener

import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.connection.util.HaveKernelContext
import com.github.xadkile.bicp.message.api.system.SystemEventReactor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.lang.Exception

/**
 * Listen for in-coming message.
 * Dispatch message to the appropriate handlers.
 */
internal sealed interface MsgListener : MsgHandlerContainer{

    /**
     * A listener may outlive the scope in which it is launch, so inject a scope in the start function.
     * This function must guarantee that when it returns the MsgListener is ready to handle incoming message, and no more waiting is needed.
     * Calling start() on an already started listener has no effect.
     */
    fun start(
        externalScope: CoroutineScope,
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ): Result<Unit, Exception>


    /**
     * stop this listener.
     * Calling stop on an already stop listener has no effect.
     */
    fun stop()

    fun isRunning(): Boolean
}
