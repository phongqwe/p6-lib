package com.emeraldblast.p6.message.api.connection.kernel_context.context_object

import com.github.michaelbull.result.Result
import com.emeraldblast.p6.common.exception.error.ErrorReport
import com.emeraldblast.p6.message.api.connection.kernel_context.KernelContextReadOnly
import com.emeraldblast.p6.message.api.message.sender.MsgSender
import com.emeraldblast.p6.message.api.message.sender.composite.CodeExecutionSender
import com.emeraldblast.p6.message.api.message.sender.composite.ExecuteResult
import com.emeraldblast.p6.message.api.message.sender.shell.*

class SenderProviderImp internal constructor(
    val kernelContext: KernelContextReadOnly,
) : SenderProvider {

    override fun executeRequestSender(): MsgSender<ExecuteRequest, Result<ExecuteReply, ErrorReport>> {
        return ExecuteSender(kernelContext)
    }


    override fun kernelInfoSender(): MsgSender<KernelInfoInput, Result<KernelInfoOutput, ErrorReport>> {
        return KernelInfoSender(kernelContext)
    }


    override fun codeExecutionSender(): MsgSender<ExecuteRequest, Result<ExecuteResult?, ErrorReport>> {
        return CodeExecutionSender(kernelContext)
    }

}
