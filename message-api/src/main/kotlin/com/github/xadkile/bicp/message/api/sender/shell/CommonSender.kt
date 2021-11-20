package com.github.xadkile.bicp.message.api.sender.shell

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatServiceConv
import com.github.xadkile.bicp.message.api.connection.ipython_context.IPythonContextReadOnly
import com.github.xadkile.bicp.message.api.connection.ipython_context.MsgEncoder
import com.github.xadkile.bicp.message.api.protocol.message.JPMessage
import com.github.xadkile.bicp.message.api.protocol.message.JPRawMessage
import com.github.xadkile.bicp.message.api.sender.ZMQMsgSender
import org.zeromq.ZContext
import org.zeromq.ZMQ
import org.zeromq.ZMsg


class CommonSender internal constructor(
)
{
    companion object {
        fun send(
            message: JPMessage<*, *>,
            socket: ZMQ.Socket,
            encoder: MsgEncoder,
            hbs: HeartBeatServiceConv,
            zContext: ZContext,
        ): Result<JPRawMessage, Exception> {
            val out: Result<ZMsg, Exception> = ZMQMsgSender.send(encoder.encodeMessage(message), socket, hbs, zContext)
            val rt: Result<JPRawMessage, Exception> = out.andThen { msg ->
                val rt: List<ByteArray> = msg.map { frame -> frame.data }
                JPRawMessage.fromPayload(rt)
            }
            return rt
        }
    }

}
