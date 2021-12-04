package com.github.xadkile.bicp.message.api.connection.kernel_context.context_object

import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.msg.listener.IOPubListener
import com.github.xadkile.bicp.message.api.msg.listener.MsgHandlerContainer
import com.github.xadkile.bicp.message.api.msg.listener.MsgListener
import com.github.xadkile.bicp.message.api.msg.protocol.JPRawMessage
import com.github.xadkile.bicp.message.api.msg.sender.MsgSender
import com.github.xadkile.bicp.message.api.msg.sender.composite.ExecuteResult
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteRequest
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteReply
import com.github.xadkile.bicp.message.api.msg.sender.shell.KernelInfoInput
import com.github.xadkile.bicp.message.api.msg.sender.shell.KernelInfoOutput

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
     * composite sender
     */
    fun codeExecutionSender(
        defaultHandler: suspend (msg: JPRawMessage, listener: MsgListener) -> Unit,
        parseExceptionHandler: suspend (exception: Exception, listener: IOPubListener) -> Unit,
    ): MsgSender<ExecuteRequest, Result<ExecuteResult, Exception>>

    /**
     * iopub listener
     */
    fun ioPubListener(
        defaultHandler: suspend (msg: JPRawMessage, listener: MsgListener) -> Unit,
        parseExceptionHandler: suspend (exception: Exception, listener: IOPubListener) -> Unit,
        parallelHandler: Boolean,
    ): MsgListener
}

