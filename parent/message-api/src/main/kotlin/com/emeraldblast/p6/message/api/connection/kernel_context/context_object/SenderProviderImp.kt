package com.emeraldblast.p6.message.api.connection.kernel_context.context_object

import com.github.michaelbull.result.Result
import com.emeraldblast.p6.common.exception.error.ErrorReport
import com.emeraldblast.p6.message.api.connection.kernel_context.KernelContextReadOnly
import com.emeraldblast.p6.message.api.message.sender.MsgSender
import com.emeraldblast.p6.message.api.message.sender.composite.CodeExecutionSender
import com.emeraldblast.p6.message.api.message.sender.composite.CodeExecutionSenderImp
import com.emeraldblast.p6.message.api.message.sender.composite.ExecuteResult
import com.emeraldblast.p6.message.api.message.sender.shell.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import javax.inject.Inject


/**
 * this class is provided using AssistedFactory to overcome circular dependency error
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
