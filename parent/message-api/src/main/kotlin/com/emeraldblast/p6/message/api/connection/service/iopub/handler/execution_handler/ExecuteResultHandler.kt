package com.emeraldblast.p6.message.api.connection.service.iopub.handler.execution_handler

import com.emeraldblast.p6.message.api.connection.service.iopub.handler.DeferredJobHandler
import com.emeraldblast.p6.message.api.message.protocol.MessageHeader
import com.emeraldblast.p6.message.api.message.sender.composite.ExecuteResult

/**
 * A handler for catching execution result
 */
interface ExecuteResultHandler : DeferredJobHandler<MessageHeader,ExecuteResult>


