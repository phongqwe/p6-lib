package com.qxdzbc.p6.message.api.connection.service.iopub.handler

import kotlinx.coroutines.CompletableDeferred
import java.util.concurrent.ConcurrentHashMap

abstract class AbsDeferredJobHandler<KEY,RESULT> : DeferredJobHandler<KEY,RESULT>, UUIDMsgHandler() {
    override val deferredJobMap: MutableMap<KEY, CompletableDeferred<RESULT>> =
        ConcurrentHashMap<KEY, CompletableDeferred<RESULT>>()

    override fun addJob(key: KEY, completableDeferred: CompletableDeferred<RESULT>) {
        deferredJobMap[key] = completableDeferred
    }

    override fun removeJob(key: KEY) {
        deferredJobMap.remove(key)
    }
}
