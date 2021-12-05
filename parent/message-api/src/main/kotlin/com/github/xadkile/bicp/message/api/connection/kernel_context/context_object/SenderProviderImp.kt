package com.github.xadkile.bicp.message.api.connection.kernel_context.context_object

import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelContextReadOnlyConv
import com.github.xadkile.bicp.message.api.connection.service.iopub.MsgListenerService
import com.github.xadkile.bicp.message.api.msg.protocol.JPRawMessage
import com.github.xadkile.bicp.message.api.msg.sender.MsgSender
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

    override fun codeExecutionSender(
        defaultHandler:  (msg: JPRawMessage) -> Unit,
        parseExceptionHandler: suspend (exception: Exception) -> Unit,
    ): MsgSender<ExecuteRequest, Result<ExecuteResult, Exception>> {
        TODO()
    }

    override fun ioPubListener(
        defaultHandler: (msg: JPRawMessage) -> Unit,
        parseExceptionHandler: suspend (exception: Exception) -> Unit,
    ): MsgListenerService {
//        return IOPubListenerService(kernelContext,
//            defaultHandler,
//            parseExceptionHandler,
//            HandlerContainerImp())
        TODO()
    }
}
