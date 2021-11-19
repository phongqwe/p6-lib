package com.github.xadkile.bicp.message.api.sender.shell

import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.connection.ipython_context.MsgEncoder
import com.github.xadkile.bicp.message.api.protocol.message.JPMessage
import com.github.xadkile.bicp.message.api.protocol.message.data_interface_definition.Shell
import com.github.xadkile.bicp.message.api.sender.MsgSender
import org.zeromq.ZMQ

typealias ExecuteRequestOutput = JPMessage<Shell.ExecuteRequest.Output.MetaData, Shell.ExecuteRequest.Output.Content>

typealias ExecuteRequestInput = JPMessage<Shell.ExecuteRequest.Input.MetaData, Shell.ExecuteRequest.Input.Content>

class ExecuteRequestSender internal constructor(
    socket: ZMQ.Socket,
    private val msgEncoder: MsgEncoder,
) : MsgSender<
        ExecuteRequestInput,
        Result<ExecuteRequestOutput,Exception>>  {

//    private val zmqMsgSender = ZMQMsgSender(socket,)

    override fun send(message: ExecuteRequestInput): Result<ExecuteRequestOutput,Exception> {
//        val zMsgResponse: ZMsg? = zmqMsgSender.send(this.msgEncoder.encodeMessage(message))
//        if(zMsgResponse!=null){
//            val rt: List<ByteArray> = zMsgResponse .map { frame->frame.data }
//            val rt2:Result<ExecuteRequestOutput,Exception> = JPMessage.fromPayload(rt)
//            return rt2
//        }else{
//            return Err(UnableToSendMsgException(message))
//        }
        TODO()
    }
}
