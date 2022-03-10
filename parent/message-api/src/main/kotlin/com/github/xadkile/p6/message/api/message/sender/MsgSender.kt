package com.github.xadkile.p6.message.api.message.sender

import com.github.xadkile.p6.message.api.message.protocol.JPMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

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
 *  It is best that MsgSender(s) are provided by [SenderProvider], which in turn, is provided by [KernelContext].
 *
 */
interface MsgSender<I : JPMessage<*, *>, O> {

    /**
     *  Sender should not outlive the scope in which it was launch, so don't inject external coroutine scope when implement this interface.
     *  Coroutine note: this function should not outlive the scope that run it, so I don't inject coroutine scope here.
     */
    suspend fun send(message: I): O

}
