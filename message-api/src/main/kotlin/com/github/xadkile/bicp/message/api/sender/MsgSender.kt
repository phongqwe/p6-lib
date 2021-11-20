package com.github.xadkile.bicp.message.api.sender

import com.github.xadkile.bicp.message.api.protocol.message.JPMessage
import com.github.xadkile.bicp.message.api.protocol.message.MsgContent
import com.github.xadkile.bicp.message.api.protocol.message.MsgMetaData
import com.github.xadkile.bicp.message.api.sender.shell.ExecuteRequestInput

/**
 * Send a JPMessage.
 *
 * A MsgSender should encapsulate:
 * - socket : where to send the message
 * - input encoder: so that input can pass security
 * - heart beat service: listening during sending
 * - converting raw byte output to output model
 *
 * Since MsgSender employ many objects whose state depends on the state of the connection to IPython, including:
 *  - socket: port, address in connection file.
 *  - encoder: key in connection file.
 *  - heart beat service: depend on port, address in connection file.
 *  It is best that MsgSender(s) are provided by [SenderProvider], which in turn, is provided by IPythonContext.
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
