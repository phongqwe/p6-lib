package com.github.xadkile.p6.message.api.connection.kernel_context.context_object

import com.github.michaelbull.result.Result
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
    fun executeRequestSender(): MsgSender<ExecuteRequest, Result<ExecuteReply, Exception>>

    /**
     * kernel info request on shell channel
     */
    fun kernelInfoSender(): MsgSender<KernelInfoInput, Result<KernelInfoOutput, Exception>>

    /**
     * composite code execution sender
     */
    fun codeExecutionSender(): MsgSender<ExecuteRequest, Result<ExecuteResult?, Exception>>

}

