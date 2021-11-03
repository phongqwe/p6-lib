package org.bitbucket.xadkile.isp.ide.jupyter.message.imp

import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message.JPMessage
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message.MsgContent
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message.MsgType
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.other.MsgIdGenerator
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.sender.MsgSender
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.session.SessionInfo
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
    val session: SessionInfo,
    val msgIdGenerator: MsgIdGenerator
) : MsgSender<I, Optional<ZMQ.Socket>> {

    init {
        session.checkLegal("Must use an OPEN Session to create ZMQMsgSender: $session")
    }

    /**
     * Return a [ZMQ.Socket] after sending a message. Return [Optional.empty] if can't send message
     */
    override fun send(msgType: MsgType, msgContent: I): Optional<ZMQ.Socket> {
        this.session.checkLegal("Must use an OPEN Session to run ZMQMsgSender.send: $session")
        val request = JPMessage.autoCreate(session, msgType, msgContent, msgIdGenerator.next())
        val payload = request.makePayload().map{ZFrame(it)}
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
