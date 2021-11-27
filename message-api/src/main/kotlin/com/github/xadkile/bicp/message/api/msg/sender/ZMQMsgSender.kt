package com.github.xadkile.bicp.message.api.msg.sender

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatService
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatServiceConv
import com.github.xadkile.bicp.message.api.connection.kernel_context.MsgEncoder
import com.github.xadkile.bicp.message.api.msg.protocol.message.JPMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.JPRawMessage
import com.github.xadkile.bicp.message.api.msg.sender.exception.UnableToQueueZMsgException
import com.github.xadkile.bicp.message.api.msg.sender.exception.ZMQMsgTimeOutException
import org.zeromq.ZContext
import org.zeromq.ZFrame
import org.zeromq.ZMQ
import org.zeromq.ZMsg

/**
 *  Sending requests to zmq.
 *  Receiving responses from zmq.'
 */
class ZMQMsgSender {

    companion object {
        /**
         * wrapper function allowing calling send function on JPMessage
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
         * - check heart beat service, only send if hb is alive
         * - send the message
         * - wait for a response using Poller with a timeout
         * [zContext] is for creating poller
         */
        fun send(
            message: List<ByteArray>,
            socket: ZMQ.Socket,
            hbs: HeartBeatServiceConv,
            zContext: ZContext,
            interval: Long = SenderConstant.defaultPollingDuration,
        ): Result<ZMsg, Exception> {
            // reminder: if heart beat service is not running, then
            // there is no way to ensure that zmq is running
            // => don't send any message if hb service is dead
            if (hbs.convCheck()) {
                val poller: ZMQ.Poller = zContext.createPoller(1)
                val payload: List<ZFrame> = message.map { ZFrame(it) }
                val zmsg: ZMsg = ZMsg().also { it.addAll(payload) }
                poller.register(socket, ZMQ.Poller.POLLIN)
                val queueOk: Boolean = zmsg.send(socket)
                if (queueOk) {
                    poller.poll(interval)
                    if (poller.pollin(0)) {
                        return Ok(ZMsg.recvMsg(socket,ZMQ.DONTWAIT))
                    }else{
                        return Err(ZMQMsgTimeOutException())
                    }
                } else {
                    return Err(UnableToQueueZMsgException(zmsg))
                }
            } else {
                return Err(HeartBeatService.NotRunningException())
            }
        }
    }
}
