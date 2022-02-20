package com.github.xadkile.p6.message.api.connection.kernel_context.context_object

import com.github.michaelbull.result.Result
import com.github.xadkile.p6.common.exception.lib.error.ErrorReport
import com.github.xadkile.p6.message.api.message.sender.MsgSender
import com.github.xadkile.p6.message.api.message.sender.composite.ExecuteResult
import com.github.xadkile.p6.message.api.message.sender.shell.ExecuteReply
import com.github.xadkile.p6.message.api.message.sender.shell.ExecuteRequest
import com.github.xadkile.p6.message.api.message.sender.shell.KernelInfoInput
import com.github.xadkile.p6.message.api.message.sender.shell.KernelInfoOutput

/**
 * provide instances of sender
 */
interface SenderProvider {
    /**
     * execute request on shell channel
     */
    fun executeRequestSender(): MsgSender<ExecuteRequest, Result<ExecuteReply, ErrorReport>>

    /**
     * kernel info request on shell channel
     */
    fun kernelInfoSender(): MsgSender<KernelInfoInput, Result<KernelInfoOutput, ErrorReport>>

    /**
     * composite code execution sender
     */
    fun codeExecutionSender(): MsgSender<ExecuteRequest, Result<ExecuteResult?, ErrorReport>>

}

