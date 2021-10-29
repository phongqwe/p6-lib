package org.bitbucket.xadkile.myide.ide.jupyter.message.imp

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rout.OutRequest
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgContentOut
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgType
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.utils.MsgIdGenerator
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.sender.MsgSender
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.session.Session
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
 *  [Session] for session info
 *  [ZMQ.Socket] for zmq connection
 *
 */
class ZMQMsgSender<I : MsgContentOut>(
    val socket: ZMQ.Socket,
    val session: Session,
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
        val request = OutRequest.autoCreate(session, msgType, msgContent, msgIdGenerator.next())
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
