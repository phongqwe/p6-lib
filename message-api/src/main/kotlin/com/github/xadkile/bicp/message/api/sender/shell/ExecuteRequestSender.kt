package com.github.xadkile.bicp.message.api.sender.shell

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatServiceConv
import com.github.xadkile.bicp.message.api.connection.ipython_context.MsgEncoder
import com.github.xadkile.bicp.message.api.protocol.message.JPMessage
import com.github.xadkile.bicp.message.api.protocol.message.JPRawMessage
import com.github.xadkile.bicp.message.api.protocol.message.data_interface_definition.Shell
import com.github.xadkile.bicp.message.api.sender.MsgSender
import com.github.xadkile.bicp.message.api.sender.ZMQMsgSender
import org.zeromq.ZContext
import org.zeromq.ZMQ

typealias ExecuteRequestOutput = JPMessage<Shell.ExecuteRequest.Output.MetaData, Shell.ExecuteRequest.Output.Content>

typealias ExecuteRequestInput = JPMessage<Shell.ExecuteRequest.Input.MetaData, Shell.ExecuteRequest.Input.Content>

/**
 */
class ExecuteRequestSender internal constructor(
    private val socket: ZMQ.Socket,
    private val msgEncoder: MsgEncoder,
    private val hbService: HeartBeatServiceConv,
    private val zContext: ZContext,
) : MsgSender<ExecuteRequestInput,
        Result<ExecuteRequestOutput, Exception>> {

    override fun send(message: ExecuteRequestInput): Result<ExecuteRequestOutput, Exception> {
        val out: Result<JPRawMessage, Exception> = ZMQMsgSender.sendJPMsg(message, socket, msgEncoder, hbService, zContext)
        val rt: Result<ExecuteRequestOutput, Exception> = out.map { msg ->
            val parsedOutput: ExecuteRequestOutput = msg.toModel()
            parsedOutput
        }
        return rt
    }
}
