package com.github.xadkile.bicp.message.api.msg.sender

import com.github.xadkile.bicp.message.api.msg.protocol.message.JPMessage

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

    /**
     * should this send function be a suspending function?
     * A suspending function is one that can suspend a coroutine scope
     * A suspending function can use coroutineScope{} inside it. That means, it automatically latched itself to the nearest coroutine scope. This ensure that whatever coroutine launched inside this function will live just as long as the outer scope.
     * If I allow scope injection, then the nested coroutine may outlive the nearest scope.
     *
     */
    fun send(message:I):O




}
