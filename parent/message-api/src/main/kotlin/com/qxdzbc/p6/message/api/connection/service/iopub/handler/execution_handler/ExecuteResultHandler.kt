package com.qxdzbc.p6.message.api.connection.service.iopub.handler.execution_handler

import com.qxdzbc.p6.message.api.connection.service.iopub.handler.DeferredJobHandler
import com.qxdzbc.p6.message.api.message.protocol.MessageHeader
import com.qxdzbc.p6.message.api.message.sender.composite.ExecuteResult

/**
 * A handler for catching execution result
 */
interface ExecuteResultHandler : DeferredJobHandler<MessageHeader,ExecuteResult>


