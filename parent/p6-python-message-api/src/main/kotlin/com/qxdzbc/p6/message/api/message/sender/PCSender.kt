package com.qxdzbc.p6.message.api.message.sender

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.qxdzbc.common.error.ErrorReport
import com.qxdzbc.p6.message.api.connection.kernel_context.context_object.MsgEncoder
import com.qxdzbc.p6.message.api.connection.service.heart_beat.HeartBeatService
import com.qxdzbc.p6.message.api.message.protocol.JPMessage
import com.qxdzbc.p6.message.api.message.protocol.JPRawMessage
import com.qxdzbc.p6.message.api.message.protocol.MsgContent
import com.qxdzbc.p6.message.api.message.protocol.MsgMetaData
import org.zeromq.ZContext
import org.zeromq.ZMQ

/**
 * encapsulate: parsing (P) and type conversion (C) of output
 */
internal class PCSender<I: JPMessage<*, *>,O: JPMessage<*, *>> internal constructor(
    val socket: ZMQ.Socket,
    val msgEncoder: MsgEncoder,
    val hbService: HeartBeatService,
    val zContext: ZContext,
    val interval: Long = SenderConstant.defaultPollingDuration,
) {

    inline fun <reified META : MsgMetaData, reified CONTENT : MsgContent>
            send(message: I): Result<O, ErrorReport> {
        return socket.use {
            val out: Result<JPRawMessage, ErrorReport> =
                ZMQMsgSender.sendJPMsg(message, socket, msgEncoder, hbService, zContext,interval)
            val rt: Result<O, ErrorReport> = out.map { msg ->
                val parsedOutput: JPMessage<*, *> = msg.toModel<META,CONTENT>()
                parsedOutput as O
            }
            rt
        }
    }
}
