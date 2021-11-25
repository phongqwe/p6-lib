package com.github.xadkile.bicp.message.api.msg.sender

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatServiceConv
import com.github.xadkile.bicp.message.api.connection.ipython_context.MsgEncoder
import com.github.xadkile.bicp.message.api.msg.protocol.message.JPMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.JPRawMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgContent
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgMetaData
import org.zeromq.ZContext
import org.zeromq.ZMQ

/**
 * encapsulate parsing and type conversion of output
 */
internal class ZSender<I: JPMessage<*, *>,O: JPMessage<*, *>> internal constructor(
    val socket: ZMQ.Socket,
    val msgEncoder: MsgEncoder,
    val hbService: HeartBeatServiceConv,
    val zContext: ZContext,
) {

    inline fun <reified META : MsgMetaData, reified CONTENT : MsgContent>
            send(message: I): Result<O, Exception> {
        val out: Result<JPRawMessage, Exception> =
            ZMQMsgSender.sendJPMsg(message, socket, msgEncoder, hbService, zContext)
        val rt: Result<O, Exception> = out.map { msg ->
            val parsedOutput: JPMessage<*, *> = msg.toModel<META,CONTENT>()
            parsedOutput as O
        }
        return rt
    }
}
