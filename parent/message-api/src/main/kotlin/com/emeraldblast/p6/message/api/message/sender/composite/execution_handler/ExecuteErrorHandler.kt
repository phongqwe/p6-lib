package com.emeraldblast.p6.message.api.message.sender.composite.execution_handler

import com.emeraldblast.p6.common.exception.error.ErrorReport

interface ExecuteErrorHandler : DeferredJobHandler<ErrorReport>
