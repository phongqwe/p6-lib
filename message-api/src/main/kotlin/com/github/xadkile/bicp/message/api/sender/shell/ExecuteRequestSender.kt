package com.github.xadkile.bicp.message.api.sender.shell

import com.github.xadkile.bicp.message.api.connection.MsgEncoder
import com.github.xadkile.bicp.message.api.protocol.message.JPMessage
import com.github.xadkile.bicp.message.api.protocol.message.data_interface_definition.Shell
import com.github.xadkile.bicp.message.api.sender.MsgSender
import com.github.xadkile.bicp.message.api.sender.ZMQMsgSender
import org.zeromq.ZMQ
import org.zeromq.ZMsg
import java.util.*
import javax.inject.Inject

typealias ExecuteRequestResponseMessage = JPMessage<Shell.ExecuteRequest.Output.MetaData, Shell.ExecuteRequest.Output.Content>
typealias ExecuteRequestInputMessage = JPMessage<Shell.ExecuteRequest.Input.MetaData, Shell.ExecuteRequest.Input.Content>

class ExecuteRequestSender @Inject constructor(
    private val socket: ZMQ.Socket,
    private val msgEncoder:MsgEncoder
) : MsgSender<
        Shell.ExecuteRequest.Input.MetaData,
        Shell.ExecuteRequest.Input.Content,
        Optional<ExecuteRequestResponseMessage>
        > {

    private val zmqMsgSender = ZMQMsgSender(socket)

    /**
     * Send a shell.code_execution message, return response msg. The response does not carry computation result, only carries status
     */
    override fun send(message: ExecuteRequestInputMessage): Optional<ExecuteRequestResponseMessage> {
        val socketResult: Optional<ZMsg> = zmqMsgSender.send(this.msgEncoder.encodeMessage(message))
        val rt: Optional<List<String>> = socketResult.map { msg ->
            val strBuilder: List<String> = msg.map { frame -> frame.getString(Charsets.UTF_8) }
            strBuilder
        }
        val rt2:Optional<ExecuteRequestResponseMessage> = rt.map {
            JPMessage.fromPayload(it)
        }
        return rt2
    }
}
