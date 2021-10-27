package org.bitbucket.xadkile.myide.ide.jupyter.message.imp.sender.zmq

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.Request
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgContent
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgType
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.sender.MsgSender
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.session.Session
import org.zeromq.ZMQ
import kotlin.jvm.Throws

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
class ZMQMsgSender<I:MsgContent>(val socket: ZMQ.Socket, val session: Session) : MsgSender<I,ZMQ.Socket> {

    init {
        session.checkLegal("Must use an OPEN Session to create ZMQMsgSender: $session")
    }
    @Throws(IllegalStateException::class)
    override fun send(msgType: MsgType, msgContent: I):ZMQ.Socket {

        this.session.checkLegal("Must use an OPEN Session to run ZMQMsgSender.send: $session")
        val request = Request.autoCreate(session, msgType, msgContent)
        val payload = request.makePayload()

        if(payload.isNotEmpty()){
            // send first n-1 piece
            for(x in 0 until payload.size-1){
                socket.sendMore(payload[x])
            }
            // send the last piece
            socket.send(payload[payload.size-1],0)
        }
        return socket
    }
}
