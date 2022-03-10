package com.github.xadkile.p6.message.api.message.sender

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.xadkile.p6.common.exception.error.CommonErrors
import com.github.xadkile.p6.common.exception.error.ErrorReport
import com.github.xadkile.p6.message.api.connection.kernel_context.context_object.MsgEncoder
import com.github.xadkile.p6.message.api.connection.service.heart_beat.HeartBeatService
import com.github.xadkile.p6.message.api.connection.service.heart_beat.errors.HBServiceErrors
import com.github.xadkile.p6.message.api.message.protocol.JPMessage
import com.github.xadkile.p6.message.api.message.protocol.JPRawMessage
import com.github.xadkile.p6.message.api.message.sender.exception.SenderErrors
import org.zeromq.ZContext
import org.zeromq.ZFrame
import org.zeromq.ZMQ
import org.zeromq.ZMsg

/**
 *  Sending requests to zmq.
 *  Receiving responses from zmq.'
 */
internal class ZMQMsgSender {

    companion object {
        /**
         * Send a JPMessage.
         */
        fun sendJPMsg(
            message: JPMessage<*, *>,
            socket: ZMQ.Socket,
            encoder: MsgEncoder,
            hbs: HeartBeatService,
            zContext: ZContext,
            interval: Long = SenderConstant.defaultPollingDuration,
        ): Result<JPRawMessage, ErrorReport> {
            val out: Result<ZMsg, ErrorReport> =
                ZMQMsgSender.send(encoder.encodeMessage(message), socket, hbs, zContext, interval)
            val rt: Result<JPRawMessage, ErrorReport> = out.andThen { msg: ZMsg ->
                val byteArrays: List<ByteArray> = msg.map { frame: ZFrame -> frame.data }
                JPRawMessage.fromPayload(byteArrays)
            }
            return rt
        }

        /**
         * Send a byte array message with this flow:
         * - check heart beat service, only send if hb is alive
         * - send the message
         * - wait for a response using Poller with a timeout
         * [zContext] is for creating poller
         */
        fun send(
            message: List<ByteArray>,
            socket: ZMQ.Socket,
            hbs: HeartBeatService,
            zContext: ZContext,
            interval: Long = SenderConstant.defaultPollingDuration,
        ): Result<ZMsg, ErrorReport> {
            // x: if heart beat service is not running, then
            // x: there is no way to ensure that zmq is running
            // x: => don't send any message if hb service is dead
            if (hbs.isServiceUpAndHBLive()) {
                val poller: ZMQ.Poller = zContext.createPoller(1)
                val payload: List<ZFrame> = message.map { ZFrame(it) }
                val zmsg: ZMsg = ZMsg().also { it.addAll(payload) }
                poller.register(socket, ZMQ.Poller.POLLIN)
                val queueOk: Boolean = zmsg.send(socket)
                if (queueOk) {
                    poller.poll(interval)
                    if (poller.pollin(0)) {
//                        return Ok(ZMsg.recvMsg(socket, ZMQ.DONTWAIT))
                        return Ok(ZMsg.recvMsg(socket))
                    } else {
                        return Err(
                            ErrorReport(
                                type = CommonErrors.TimeOut,
                                data = CommonErrors.TimeOut.Data(""),
                                loc = ""
                            )
                        )
                    }
                } else {
                    val report = ErrorReport(
                        type = SenderErrors.UnableToQueueZMsg,
                        data = SenderErrors.UnableToQueueZMsg.Data(zmsg)
                    )
                    return Err(report)
                }
            } else {
                val report = ErrorReport(
                    type = HBServiceErrors.HBIsDead,
                    data = HBServiceErrors.HBIsDead.Data("ZMQMsgSender can't send msg because hb service is not running")
                )
                return Err(report)
            }
        }

        /**
         * send a [message] but don't wait for reply.
         */
        fun sendJPMsgNoReply(
            message: JPMessage<*, *>,
            socket: ZMQ.Socket,
            encoder: MsgEncoder,
            hbs: HeartBeatService,
        ): Result<Unit, ErrorReport> {
            val out: Result<Unit, ErrorReport> = ZMQMsgSender.sendNoReply(encoder.encodeMessage(message), socket, hbs)
            return out
        }

        /**
         * send a [message] but don't wait for reply.
         */
        fun sendNoReply(
            message: List<ByteArray>,
            socket: ZMQ.Socket,
            hbs: HeartBeatService,
        ): Result<Unit, ErrorReport> {
            // x: if heart beat service is not running, then
            // x: there is no way to ensure that zmq is running
            // x: => don't send any message if hb service is dead
            if (hbs.isServiceUpAndHBLive()) {
                val payload: List<ZFrame> = message.map { ZFrame(it) }
                val zmsg: ZMsg = ZMsg().also { it.addAll(payload) }
                val queueOk: Boolean = zmsg.send(socket)
                if (queueOk) {
                    return Ok(Unit)

                } else {
                    val report = ErrorReport(
                        type = SenderErrors.UnableToQueueZMsg,
                        data = SenderErrors.UnableToQueueZMsg.Data(zmsg)
                    )
                    return Err(report)
                }
            } else {
                val report = ErrorReport(
                    type = HBServiceErrors.HBIsDead,
                    data = HBServiceErrors.HBIsDead.Data("ZMQMsgSender can't send msg because hb service is not running")
                )
                return Err(report)
            }
        }
    }
}
