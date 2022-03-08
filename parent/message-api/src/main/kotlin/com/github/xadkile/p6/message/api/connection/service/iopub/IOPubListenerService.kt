package com.github.xadkile.p6.message.api.connection.service.iopub

import com.github.michaelbull.result.Result
import com.github.xadkile.p6.common.exception.error.ErrorReport
import com.github.xadkile.p6.message.api.connection.service.Service
import com.github.xadkile.p6.message.api.message.protocol.MsgType

/**
 * Listen for in-coming message.
 * Dispatch message to the appropriate handlers.
 */
interface IOPubListenerService : MsgHandlerContainer, Service{

    /**
     * A listener may outlive the scope in which it is launch, so inject a scope in the start function.
     * This function must guarantees that when it returns the MsgListener is ready to handle incoming message, and no more waiting is needed.
     * Calling start() on an already started listener doesn't do anything.
     */
    override suspend fun start(): Result<Unit, ErrorReport>


    /**
     * Stop this listener. This method guarantees that this listener is completely stopped after this method returns.
     * Calling stop on an already stop listener doesn't do anything.
     */
    override suspend fun stop():Result<Unit, ErrorReport>

    fun addDefaultHandler(handler: MsgHandler){
        if(handler.msgType == MsgType.DEFAULT){
            this.addHandler(handler)
        }else{
            throw RuntimeException("only used this method to add default handler")
        }
    }
}

