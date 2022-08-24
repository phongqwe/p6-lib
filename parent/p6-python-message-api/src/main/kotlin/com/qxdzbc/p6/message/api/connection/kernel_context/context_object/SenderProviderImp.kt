package com.qxdzbc.p6.message.api.connection.kernel_context.context_object

import com.github.michaelbull.result.Result
import com.qxdzbc.common.error.ErrorReport
import com.qxdzbc.p6.message.api.message.sender.MsgSender
import com.qxdzbc.p6.message.api.message.sender.composite.CodeExecutionSender
import com.qxdzbc.p6.message.api.message.sender.composite.ExecuteResult
import com.qxdzbc.p6.message.api.message.sender.shell.*
import dagger.assisted.AssistedInject


/**
 * Important: this class is provided using AssistedFactory to overcome circular dependency error
 */
class SenderProviderImp @AssistedInject constructor(
    private val executeSender:ExecuteSender,
    private val kernelInfoSender: KernelInfoSender,
    private val codeExecutionSender: CodeExecutionSender,
) : SenderProvider {

    override fun executeRequestSender(): MsgSender<ExecuteRequest, Result<ExecuteReply, ErrorReport>> {
        return executeSender
    }

    override fun kernelInfoSender(): MsgSender<KernelInfoInput, Result<KernelInfoOutput, ErrorReport>> {
        return kernelInfoSender
    }


    override fun codeExecutionSender(): MsgSender<ExecuteRequest, Result<ExecuteResult?, ErrorReport>> {
//        return CodeExecutionSenderImp(kernelContext)
        return codeExecutionSender
    }

}
