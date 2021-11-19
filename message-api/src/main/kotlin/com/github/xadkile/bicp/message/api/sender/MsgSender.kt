package com.github.xadkile.bicp.message.api.sender

import com.github.xadkile.bicp.message.api.protocol.message.JPMessage
import com.github.xadkile.bicp.message.api.protocol.message.MsgContent
import com.github.xadkile.bicp.message.api.protocol.message.MsgMetaData
import com.github.xadkile.bicp.message.api.sender.shell.ExecuteRequestInput

/**
 * Send a JPMessage
 */
interface MsgSender<I:JPMessage<*,*>,O> {
    fun send(message:I):O

    class UnableToSendMsgException(
        val msg: JPMessage<*,*>
    ) : Exception()

    class UnableToQueueMsgException(
        val msg: JPMessage<*,*>
    ):Exception()
}
