package com.github.xadkile.bicp.message.api.msg.sender.shell

import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatServiceConv
import com.github.xadkile.bicp.message.api.connection.ipython_context.MsgEncoder
import com.github.xadkile.bicp.message.api.msg.protocol.message.JPMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition.Shell
import com.github.xadkile.bicp.message.api.msg.sender.MsgSender
import com.github.xadkile.bicp.message.api.msg.sender.ZSender
import org.zeromq.ZContext
import org.zeromq.ZMQ

typealias ExecuteRequest = JPMessage<Shell.Execute.Reply.MetaData, Shell.Execute.Reply.Content>

typealias ExecuteReply = JPMessage<Shell.Execute.Request.MetaData, Shell.Execute.Request.Content>

/**
 * [zContext] is for creating poller
 */
class ExecuteSender internal constructor(
    socket: ZMQ.Socket,
    msgEncoder: MsgEncoder,
    hbService: HeartBeatServiceConv,
    zContext: ZContext,
) : MsgSender<ExecuteReply,
        Result<ExecuteRequest, Exception>> {

    private val zsender = ZSender<ExecuteReply,ExecuteRequest>(socket, msgEncoder, hbService, zContext)
    override fun send(message: ExecuteReply): Result<ExecuteRequest, Exception> {
        val rt = zsender.send<Shell.Execute.Reply.MetaData,Shell.Execute.Reply.Content>(message)
        return rt
    }
}
