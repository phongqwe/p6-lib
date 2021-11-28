package com.github.xadkile.bicp.message.api.msg.sender

import com.github.xadkile.bicp.message.api.connection.util.HaveKernelContext
import com.github.xadkile.bicp.message.api.msg.protocol.message.JPMessage
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
interface MsgSender<I : JPMessage<*, *>, O> : HaveKernelContext {

    /**
     *  Sender should not outlive the scope in which it was launch, so don't inject external coroutine scope when implement this interface
     */
    suspend fun send(message: I, dispatcher: CoroutineDispatcher = Dispatchers.IO): O

}
