package com.github.xadkile.bicp.message.api.sender

import org.zeromq.ZFrame
import org.zeromq.ZMQ
import org.zeromq.ZMsg
import java.util.*

/**
 *
 *  Sending requests to zmq.
 *  Receiving responses to zmq
 */
class ZMQMsgSender(
    private val socket: ZMQ.Socket,
)  {

    /**
     * Return a [ZMQ.Socket] after sending a message. Return [Optional.empty] if can't send message
     */
    fun send(message: List<ByteArray>): ZMsg? {
        val payload:List<ZFrame> = message.map{ZFrame(it)}
        val zmsg = ZMsg().also {
            it.addAll(payload)
        }
        val sendOk:Boolean = zmsg.send(socket)
        if(sendOk){
            return ZMsg.recvMsg(socket)
        }else{
            return null
        }
    }
}
