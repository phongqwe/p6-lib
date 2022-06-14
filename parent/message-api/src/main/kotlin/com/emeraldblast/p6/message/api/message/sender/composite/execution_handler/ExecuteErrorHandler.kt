package com.emeraldblast.p6.message.api.message.sender.composite.execution_handler

import com.emeraldblast.p6.common.exception.error.ErrorReport
/**
 * A handler for catching execution errors
 */
interface ExecuteErrorHandler : DeferredJobHandler<ErrorReport>
