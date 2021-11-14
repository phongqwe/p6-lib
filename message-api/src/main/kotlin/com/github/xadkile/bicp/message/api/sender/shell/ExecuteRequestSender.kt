package com.github.xadkile.bicp.message.api.sender.shell

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.connection.MsgEncoder
import com.github.xadkile.bicp.message.api.exception.UnableToSendMsgException
import com.github.xadkile.bicp.message.api.protocol.MessageHeader
import com.github.xadkile.bicp.message.api.protocol.message.JPMessage
import com.github.xadkile.bicp.message.api.protocol.message.data_interface_definition.Shell
import com.github.xadkile.bicp.message.api.sender.MsgSender
import com.github.xadkile.bicp.message.api.sender.ZMQMsgSender
import org.zeromq.ZMQ
import org.zeromq.ZMsg
import javax.inject.Inject

typealias ExecuteRequestOutputMessage = JPMessage<Shell.ExecuteRequest.Output.MetaData, Shell.ExecuteRequest.Output.Content>
typealias ExecuteRequestInputMessage = JPMessage<Shell.ExecuteRequest.Input.MetaData, Shell.ExecuteRequest.Input.Content>

class ExecuteRequestSender @Inject internal constructor(
    private val socket: ZMQ.Socket,
    private val msgEncoder:MsgEncoder
) : MsgSender<
        ExecuteRequestInputMessage,
        Result<ExecuteRequestOutputMessage,Exception>>  {

    private val zmqMsgSender = ZMQMsgSender(socket)

    override fun send(message: ExecuteRequestInputMessage): Result<ExecuteRequestOutputMessage,Exception> {
        val r1: ZMsg? = zmqMsgSender.send(this.msgEncoder.encodeMessage(message))
        if(r1!=null){
            val rt: List<String> = r1 .map { frame->frame.getString(Charsets.UTF_8) }
            val rt2:ExecuteRequestOutputMessage = JPMessage.fromPayload(rt)
            return Ok(rt2)
        }else{
            return Err(UnableToSendMsgException(message))
        }
    }
}
