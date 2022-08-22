package com.qxdzbc.p6.message.api.connection.service.iopub

import com.github.michaelbull.result.Result
import com.qxdzbc.p6.common.exception.error.ErrorReport
import com.qxdzbc.p6.message.api.connection.service.Service
import com.qxdzbc.p6.message.api.connection.service.iopub.handler.MsgHandler
import com.qxdzbc.p6.message.api.connection.service.iopub.handler.execution_handler.*
import com.qxdzbc.p6.message.api.message.protocol.MsgType

/**
 * Listen for in-coming message on iopub channel.
 * Dispatch message to the appropriate handlers which are categorized by the type of message that they can handle.
 * This listener service includes standards handlers that are exposed as properties.
 * This listener accepts additional handlers, add them using [addHandler] or [addHandlers] method.
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
    override suspend fun stopJoin():Result<Unit, ErrorReport>

    fun addDefaultHandler(handler: MsgHandler){
        if(handler.msgType == MsgType.DEFAULT){
            this.addHandler(handler)
        }else{
            throw RuntimeException("only used this method to add default handler")
        }
    }

    val executeResultHandler: ExecuteResultHandler
    val executeErrorHandler: ExecuteErrorHandler
    val idleExecutionStatusHandler : IdleExecutionStatusHandler
    val busyExecutionStatusHandler : BusyExecutionStatusHandler
    val displayDataHandler: DisplayDataHandler
}

