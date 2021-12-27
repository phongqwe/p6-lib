package com.github.xadkile.p6.message.api.connection.kernel_context.context_object

import com.github.michaelbull.result.Result
import com.github.xadkile.p6.exception.error.ErrorReport
import com.github.xadkile.p6.message.api.connection.service.iopub.IOPubListenerService
import com.github.xadkile.p6.message.api.msg.protocol.JPRawMessage
import com.github.xadkile.p6.message.api.msg.sender.MsgSender
import com.github.xadkile.p6.message.api.msg.sender.composite.ExecuteResult
import com.github.xadkile.p6.message.api.msg.sender.shell.ExecuteRequest
import com.github.xadkile.p6.message.api.msg.sender.shell.ExecuteReply
import com.github.xadkile.p6.message.api.msg.sender.shell.KernelInfoInput
import com.github.xadkile.p6.message.api.msg.sender.shell.KernelInfoOutput

/**
 * provide instances of sender
 */
interface SenderProvider {
    /**
     * execute request on shell channel
     */
    fun executeRequestSender2(): MsgSender<ExecuteRequest, Result<ExecuteReply, ErrorReport>>

    /**
     * kernel info request on shell channel
     */
    fun kernelInfoSender2(): MsgSender<KernelInfoInput, Result<KernelInfoOutput, ErrorReport>>

    /**
     * composite code execution sender
     */
    fun codeExecutionSender2(): MsgSender<ExecuteRequest, Result<ExecuteResult?, ErrorReport>>

}

