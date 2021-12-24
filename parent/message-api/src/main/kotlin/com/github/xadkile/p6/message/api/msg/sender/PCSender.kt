package com.github.xadkile.p6.message.api.msg.sender

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.xadkile.p6.message.api.connection.kernel_context.context_object.MsgEncoder
import com.github.xadkile.p6.message.api.connection.service.heart_beat.HeartBeatServiceConv
import com.github.xadkile.p6.message.api.msg.protocol.JPMessage
import com.github.xadkile.p6.message.api.msg.protocol.JPRawMessage
import com.github.xadkile.p6.message.api.msg.protocol.MsgContent
import com.github.xadkile.p6.message.api.msg.protocol.MsgMetaData
import org.zeromq.ZContext
import org.zeromq.ZMQ

/**
 * encapsulate: parsing (P) and type conversion (C) of output
 */
internal class PCSender<I: JPMessage<*, *>,O: JPMessage<*, *>> internal constructor(
    val socket: ZMQ.Socket,
    val msgEncoder: MsgEncoder,
    val hbService: HeartBeatServiceConv,
    val zContext: ZContext,
    val interval: Long = SenderConstant.defaultPollingDuration,
) {

    inline fun <reified META : MsgMetaData, reified CONTENT : MsgContent>
            send(message: I): Result<O, Exception> {
        return socket.use {
            val out: Result<JPRawMessage, Exception> =
                ZMQMsgSender.sendJPMsg(message, socket, msgEncoder, hbService, zContext,interval)
            val rt: Result<O, Exception> = out.map { msg ->
                val parsedOutput: JPMessage<*, *> = msg.toModel<META,CONTENT>()
                parsedOutput as O
            }
            rt
        }
    }
}
