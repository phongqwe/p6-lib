package com.github.xadkile.bicp.message.api.connection.kernel_context.context_object

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.unwrap
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelContextReadOnlyConv
import com.github.xadkile.bicp.message.api.connection.service.iopub.IOPubListenerService
import com.github.xadkile.bicp.message.api.msg.protocol.JPRawMessage
import com.github.xadkile.bicp.message.api.msg.sender.MsgSender
import com.github.xadkile.bicp.message.api.msg.sender.composite.CodeExecutionSender
import com.github.xadkile.bicp.message.api.msg.sender.composite.ExecuteResult
import com.github.xadkile.bicp.message.api.msg.sender.shell.*

class SenderProviderImp internal constructor(
    val kernelContext: KernelContextReadOnlyConv,
) : SenderProvider {

    override fun executeRequestSender(): MsgSender<ExecuteRequest, Result<ExecuteReply, Exception>> {
        return ExecuteSender(kernelContext)
    }

    override fun kernelInfoSender(): MsgSender<KernelInfoInput, Result<KernelInfoOutput, Exception>> {
        return KernelInfoSender(kernelContext)
    }

    override fun codeExecutionSender(): MsgSender<ExecuteRequest, Result<ExecuteResult, Exception>> {
        return CodeExecutionSender(kernelContext)
    }
}
