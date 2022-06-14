package com.emeraldblast.p6.message.api.connection.service.iopub.handler.execution_handler

import com.emeraldblast.p6.common.exception.error.ErrorReport
import com.emeraldblast.p6.message.api.connection.service.iopub.handler.DeferredJobHandler
import com.emeraldblast.p6.message.api.message.protocol.MessageHeader

/**
 * A handler for catching execution errors
 */
interface ExecuteErrorHandler : DeferredJobHandler<MessageHeader,ErrorReport>
