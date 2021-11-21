package com.github.xadkile.bicp.message.api.msg.sender

import com.github.michaelbull.result.*
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatService
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatServiceConv
import com.github.xadkile.bicp.message.api.connection.ipython_context.IPythonContext
import com.github.xadkile.bicp.message.api.connection.ipython_context.IPythonContextReadOnly
import com.github.xadkile.bicp.message.api.connection.ipython_context.MsgEncoder
import com.github.xadkile.bicp.message.api.exception.UnknownException
import com.github.xadkile.bicp.message.api.protocol.message.JPMessage
import com.github.xadkile.bicp.message.api.protocol.message.JPRawMessage
import org.zeromq.ZContext
import org.zeromq.ZFrame
import org.zeromq.ZMQ
import org.zeromq.ZMsg

/**
 *  Sending requests to zmq.
 *  Receiving responses to zmq
 */
class ZMQMsgSender {

    class UnableToQueueException : Exception()

    companion object {
        /**
         * [zContext] is for creating poller
         */
        fun sendJPMsg(
            message: JPMessage<*, *>,
            socket: ZMQ.Socket,
            encoder: MsgEncoder,
            hbs: HeartBeatServiceConv,
            zContext: ZContext,
        ): Result<JPRawMessage, Exception> {
            val out: Result<ZMsg, Exception> = send(encoder.encodeMessage(message), socket, hbs, zContext)
            val rt: Result<JPRawMessage, Exception> = out.andThen { msg ->
                val rt: List<ByteArray> = msg.map { frame -> frame.data }
                JPRawMessage.fromPayload(rt)
            }
            return rt
        }

        /**
         * Send a message with this flow:
         * - send the message
         * - wait for a response using Poller with a timeout
         * - check heart beat after each poll
         * - continue until either receiving a response or the zmq is dead
         * [zContext] is for creating poller
         */
        fun send(
            message: List<ByteArray>,
            socket: ZMQ.Socket,
            hbs:HeartBeatServiceConv,
            zContext:ZContext,
            interval: Long = SenderConstant.defaultPollingDuration
        ): Result<ZMsg,Exception> {
                // reminder: if heart beat service is not running, then
                // there is no way to ensure that zmq is running
                // => don't send any message if hb service is dead
                if (hbs.convCheck()) {
                    val poller: ZMQ.Poller = zContext.createPoller(1)
                    val payload: List<ZFrame> = message.map { ZFrame(it) }
                    val zmsg:ZMsg = ZMsg().also { it.addAll(payload) }
                    poller.register(socket)
                    val queueOk: Boolean = zmsg.send(socket)
                    if (queueOk) {
                        var recvMsg: ZMsg? = null
                        while (hbs.convCheck() && recvMsg == null) {
                            val i: Int = poller.poll(interval)
                            if (i == 1) {
                                recvMsg = ZMsg.recvMsg(socket)
                                break
                            }
                        }
                        if(recvMsg!=null){
                            return Ok(recvMsg)
                        }else{
                            return Err(HeartBeatService.ZMQIsDeadException())
                        }
                    } else {
                        return Err(UnableToQueueException())
                    }
                } else {
                    return Err(HeartBeatService.NotRunningException())
                }
        }
    }
}
