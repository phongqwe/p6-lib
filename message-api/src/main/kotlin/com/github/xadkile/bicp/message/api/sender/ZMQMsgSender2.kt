package com.github.xadkile.bicp.message.api.sender

import com.github.xadkile.bicp.message.api.protocol.message.JPMessage
import com.github.xadkile.bicp.message.api.protocol.message.MsgContent
import com.github.xadkile.bicp.message.api.connection.SessionInfo
import com.github.xadkile.bicp.message.api.protocol.message.MsgMetaData
import org.zeromq.ZFrame
import org.zeromq.ZMQ
import org.zeromq.ZMsg
import java.util.*

/**
 * handle:
 *  creating request
 *  send the request's payload to zma
 *  this class does not handle recv, but return the socket right after sending
 * depend on:
 *  [SessionInfo] for session info
 *  [ZMQ.Socket] for zmq connection
 *
 */
class ZMQMsgSender2<M: MsgMetaData,C: MsgContent>(
    private val socket: ZMQ.Socket,
) : MsgSender2< M, C, Optional<ZMsg>> {

    /**
     * Return a [ZMQ.Socket] after sending a message. Return [Optional.empty] if can't send message
     */
    override fun send(message: JPMessage<M, C>): Optional<ZMsg> {
        val payload:List<ZFrame> = message.makePayload().map{ZFrame(it)}
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
