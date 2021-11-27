package com.github.xadkile.bicp.message.api.connection.kernel_context

import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.msg.sender.MsgSender
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteRequest
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteReply
import com.github.xadkile.bicp.message.api.msg.sender.shell.KernelInfoInput
import com.github.xadkile.bicp.message.api.msg.sender.shell.KernelInfoOutput

/**
 * provide instances of sender
 */
interface SenderProvider {
    fun getExecuteRequestSender(): MsgSender<ExecuteRequest, Result<ExecuteReply, Exception>>
    fun getKernelInfoSender():MsgSender<KernelInfoInput,Result<KernelInfoOutput,Exception>>
}

