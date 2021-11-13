package com.github.xadkile.bicp.message.api.sender

import com.github.xadkile.bicp.message.api.connection.SessionInfo
import org.zeromq.ZFrame
import org.zeromq.ZMQ
import org.zeromq.ZMsg
import java.util.*

/**
 *
 * handle:
 *  sending requests to zmq
 *  receiving responses to zmq
 *  [ZMQ.Socket] for zmq connection
 *
 */
class ZMQMsgSender(
    private val socket: ZMQ.Socket,
)  {

    /**
     * Return a [ZMQ.Socket] after sending a message. Return [Optional.empty] if can't send message
     */
    fun send(message: List<ByteArray>): Optional<ZMsg> {
        val payload:List<ZFrame> = message.map{ZFrame(it)}
        val zmsg = ZMsg().also {
            it.addAll(payload)
        }
        val sendOk:Boolean = zmsg.send(socket)
        val rt:Optional<ZMsg> = if (sendOk) {
            Optional.of(socket)
        } else {
            Optional.empty()
        }.map { ZMsg.recvMsg(it) }
        return rt
    }
}
