package com.github.xadkile.bicp.message.api.connection.ipython_context

import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.msg.sender.MsgSender
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteRequestInput
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteRequestOutput
import com.github.xadkile.bicp.message.api.msg.sender.shell.KernelInfoInput
import com.github.xadkile.bicp.message.api.msg.sender.shell.KernelInfoOutput

/**
 * provide instances of sender
 */
interface SenderProvider {
    fun getExecuteRequestSender(): MsgSender<ExecuteRequestInput, Result<ExecuteRequestOutput, Exception>>
    fun getKernelInfoSender():MsgSender<KernelInfoInput,Result<KernelInfoOutput,Exception>>
}

