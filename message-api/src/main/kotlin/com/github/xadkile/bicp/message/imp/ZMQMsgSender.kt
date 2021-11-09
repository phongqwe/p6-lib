package com.github.xadkile.bicp.message.imp

import com.github.xadkile.bicp.message.api.protocol.message.JPMessage
import com.github.xadkile.bicp.message.api.protocol.message.MsgContent
import com.github.xadkile.bicp.message.api.protocol.message.MsgType
import com.github.xadkile.bicp.message.api.protocol.other.MsgIdGenerator
import com.github.xadkile.bicp.message.api.sender.MsgSender
import com.github.xadkile.bicp.message.api.connection.SessionInfo
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
class ZMQMsgSender<I : MsgContent>(
    val socket: ZMQ.Socket,
    val sessionInfo: SessionInfo,
    val msgIdGenerator: MsgIdGenerator
) : MsgSender<I, Optional<ZMQ.Socket>> {

    init {
        sessionInfo.checkLegal("Must use an OPEN Session to create ZMQMsgSender: $sessionInfo")
    }

    /**
     * Return a [ZMQ.Socket] after sending a message. Return [Optional.empty] if can't send message
     */
    override fun send(msgType: MsgType, msgContent: I): Optional<ZMQ.Socket> {
        this.sessionInfo.checkLegal("Must use an OPEN Session to run ZMQMsgSender.send: $sessionInfo")
        val message = JPMessage.autoCreate(sessionInfo, msgType, msgContent, msgIdGenerator.next())
        val payload = message.makePayload().map{ZFrame(it)}
        val zmsg = ZMsg().also {
            it.addAll(payload)
        }
        val sendOk:Boolean = zmsg.send(socket)
        val rt:Optional<ZMQ.Socket> = if (sendOk) {
            Optional.of(socket)
        } else {
            Optional.empty()
        }
        return rt
    }
}
