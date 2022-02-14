package com.github.xadkile.p6.message.api.connection.kernel_context.context_object

import com.github.xadkile.p6.message.api.message.protocol.JPMessage

/**
 * Encode a message into approriate zmq format.
 * An encoder must used a key read from a a valid connection file so that the message can be parsed correctly on the backend.
 */
interface MsgEncoder {
    fun encodeMessage(message: JPMessage<*, *>): List<ByteArray>
}

