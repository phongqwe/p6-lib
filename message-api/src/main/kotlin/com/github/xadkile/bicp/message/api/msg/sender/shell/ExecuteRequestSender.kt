package com.github.xadkile.bicp.message.api.msg.sender.shell

import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatServiceConv
import com.github.xadkile.bicp.message.api.connection.ipython_context.MsgEncoder
import com.github.xadkile.bicp.message.api.protocol.message.JPMessage
import com.github.xadkile.bicp.message.api.protocol.message.data_interface_definition.Shell
import com.github.xadkile.bicp.message.api.msg.sender.MsgSender
import com.github.xadkile.bicp.message.api.msg.sender.ZSender
import org.zeromq.ZContext
import org.zeromq.ZMQ

typealias ExecuteRequestOutput = JPMessage<Shell.ExecuteRequest.Output.MetaData, Shell.ExecuteRequest.Output.Content>

typealias ExecuteRequestInput = JPMessage<Shell.ExecuteRequest.Input.MetaData, Shell.ExecuteRequest.Input.Content>

/**
 * [zContext] is for creating poller
 */
class ExecuteRequestSender internal constructor(
    socket: ZMQ.Socket,
    msgEncoder: MsgEncoder,
    hbService: HeartBeatServiceConv,
    zContext: ZContext,
) : MsgSender<ExecuteRequestInput,
        Result<ExecuteRequestOutput, Exception>> {

    private val osender = ZSender<ExecuteRequestInput,ExecuteRequestOutput>(socket, msgEncoder, hbService, zContext)
    override fun send(message: ExecuteRequestInput): Result<ExecuteRequestOutput, Exception> {
        val rt = osender.send<Shell.ExecuteRequest.Output.MetaData,Shell.ExecuteRequest.Output.Content>(message)
        return rt
    }
}
