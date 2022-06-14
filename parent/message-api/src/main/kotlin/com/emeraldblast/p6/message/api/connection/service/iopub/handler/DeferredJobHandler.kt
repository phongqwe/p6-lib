package com.emeraldblast.p6.message.api.connection.service.iopub.handler

import kotlinx.coroutines.CompletableDeferred

/**
 * A deferred job handler is a MsgHandler that deliver/emmit side effect using deferred jobs
 */
interface DeferredJobHandler <KEY,RESULT> : MsgHandler {
    val deferredJobMap:MutableMap<KEY, CompletableDeferred<RESULT>>
    fun addJob(key: KEY, completableDeferred: CompletableDeferred<RESULT>)
    fun removeJob(key: KEY)
}

