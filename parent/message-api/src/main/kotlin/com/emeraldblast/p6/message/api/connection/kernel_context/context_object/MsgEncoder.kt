package com.emeraldblast.p6.message.api.connection.kernel_context.context_object

import com.emeraldblast.p6.message.api.message.protocol.JPMessage

/**
 * Encode a message into approriate zmq format.
 * An encoder must use a key read from a valid connection file so that the message can be parsed correctly on the backend.
 */
interface MsgEncoder {
    fun encodeMessage(message: JPMessage<*, *>): List<ByteArray>
}

