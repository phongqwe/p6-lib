package org.bitbucket.xadkile.myide.ide.jupyter.message.imp.sender.zmq

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.Request
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgContent
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgType
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.utils.MsgIdGenerator
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.sender.MsgSender
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.session.Session
import org.zeromq.ZFrame
import org.zeromq.ZMQ
import org.zeromq.ZMsg

/**
 * handle:
 *  creating request
 *  send the request's payload to zma
 *  dont call recv on the socket and return the socket
 * depend on:
 *  [Session] for session info
 *  [ZMQ.Socket] for zmq connection
 *
 */
class ZMQMsgSender<I : MsgContent>(
    val socket: ZMQ.Socket,
    val session: Session,
    val msgIdGenerator: MsgIdGenerator
) : MsgSender<I, Option<ZMQ.Socket>> {

    init {
        session.checkLegal("Must use an OPEN Session to create ZMQMsgSender: $session")
    }

    override fun send(msgType: MsgType, msgContent: I): Option<ZMQ.Socket> {

        this.session.checkLegal("Must use an OPEN Session to run ZMQMsgSender.send: $session")
        val request = Request.autoCreate(session, msgType, msgContent, msgIdGenerator.next())
        val payload = request.toFacade().makeSendPayload().map{ZFrame(it)}
        val zmsg = ZMsg().also {
            it.addAll(payload)
        }
        val sendOk:Boolean = zmsg.send(socket)
        val rt = if (sendOk) {
//            Either.Right(socket)
            Some(socket)
        } else {
//            Either.Left (CantSendMsgException("can't send message: ${request.getMsgId()} of session ${request.getSessionId()}"))
            None
        }
        return rt
    }
}
