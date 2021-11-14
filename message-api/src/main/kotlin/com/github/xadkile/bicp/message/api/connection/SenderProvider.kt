package com.github.xadkile.bicp.message.api.connection

import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.sender.MsgSender
import com.github.xadkile.bicp.message.api.sender.shell.ExecuteRequestInputMessage
import com.github.xadkile.bicp.message.api.sender.shell.ExecuteRequestOutputMessage

/**
 * provide instances of sender
 */
interface SenderProvider {
    fun getSingletonExecuteRequestSender(): MsgSender<ExecuteRequestInputMessage, Result<ExecuteRequestOutputMessage,Exception>>
    fun getNewExecuteRequestSender(): MsgSender<ExecuteRequestInputMessage, Result<ExecuteRequestOutputMessage,Exception>>
}

