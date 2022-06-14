package com.emeraldblast.p6.message.api.message.sender.composite.execution_handler

import com.emeraldblast.p6.message.api.connection.service.iopub.MsgHandler
import com.emeraldblast.p6.message.api.message.protocol.MessageHeader
import kotlinx.coroutines.CompletableDeferred

/**
 * A deferred job handler is a MsgHandler that deliver/emmit side effect using deferred jobs
 */
interface DeferredJobHandler <T> : MsgHandler {
    val deferredJobMap:MutableMap<MessageHeader, CompletableDeferred<T>>
    fun addJob(msgHeader: MessageHeader, completableDeferred: CompletableDeferred<T>)
    fun removeJob(msgHeader: MessageHeader)
}

