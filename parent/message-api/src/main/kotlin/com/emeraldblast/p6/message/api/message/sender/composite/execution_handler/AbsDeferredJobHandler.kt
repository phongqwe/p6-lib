package com.emeraldblast.p6.message.api.message.sender.composite.execution_handler

import com.emeraldblast.p6.message.api.connection.service.iopub.UUIDMsgHandler
import com.emeraldblast.p6.message.api.message.protocol.MessageHeader
import kotlinx.coroutines.CompletableDeferred
import java.util.concurrent.ConcurrentHashMap

abstract class AbsDeferredJobHandler <T> : DeferredJobHandler<T>, UUIDMsgHandler(){
    override val deferredJobMap:MutableMap<MessageHeader, CompletableDeferred<T>> = ConcurrentHashMap<MessageHeader, CompletableDeferred<T>>()
    override fun addJob(msgHeader: MessageHeader, completableDeferred: CompletableDeferred<T>){
        deferredJobMap[msgHeader] = completableDeferred
    }

    override fun removeJob(msgHeader: MessageHeader) {
        deferredJobMap.remove(msgHeader)
    }
}
