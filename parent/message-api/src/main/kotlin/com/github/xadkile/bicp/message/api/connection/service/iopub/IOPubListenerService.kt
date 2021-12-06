package com.github.xadkile.bicp.message.api.connection.service.iopub

import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.msg.protocol.MsgType
import com.github.xadkile.bicp.message.api.other.RunningState
import java.lang.Exception

/**
 * Listen for in-coming message.
 * Dispatch message to the appropriate handlers.
 */
interface IOPubListenerService : IOPubListenerServiceReadOnly{

    /**
     * A listener may outlive the scope in which it is launch, so inject a scope in the start function.
     * This function must guarantees that when it returns the MsgListener is ready to handle incoming message, and no more waiting is needed.
     * Calling start() on an already started listener doesn't do anything.
     */
    fun start(): Result<Unit, Exception>


    /**
     * Stop this listener. This method guarantees that this listener is completely stopped after this method returns.
     * Calling stop on an already stop listener doesn't do anything.
     */
    suspend fun stop()

    fun toReadOnly():IOPubListenerServiceReadOnly{
        return this
    }
}

interface IOPubListenerServiceReadOnly : MsgHandlerContainer,RunningState{
    fun addDefaultHandler(handler:MsgHandler){
        if(handler.msgType() == MsgType.DEFAULT){
            this.addHandler(handler)
        }else{
            throw RuntimeException("only used this method to add default handler")
        }
    }
}
