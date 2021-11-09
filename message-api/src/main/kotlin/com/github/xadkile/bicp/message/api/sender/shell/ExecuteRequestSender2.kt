package com.github.xadkile.bicp.message.api.sender.shell

import com.github.xadkile.bicp.message.api.protocol.message.JPMessage
import com.github.xadkile.bicp.message.api.protocol.message.data_interface_definition.Shell
import com.github.xadkile.bicp.message.api.sender.MsgSender2
import com.github.xadkile.bicp.message.api.sender.ZMQMsgSender2
import org.zeromq.ZMQ
import org.zeromq.ZMsg
import java.util.*
import javax.inject.Inject


class ExecuteRequestSender2 @Inject constructor(
    socket: ZMQ.Socket,
) : MsgSender2<Shell.ExecuteRequest.Out.MetaData,Shell.ExecuteRequest.Out.Content,
        Optional<JPMessage<Shell.ExecuteRequest.In.MetaData, Shell.ExecuteRequest.In.Content>>> {

    private val zmqMsgSender =
        ZMQMsgSender2<Shell.ExecuteRequest.Out.MetaData,Shell.ExecuteRequest.Out.Content>(socket)

    /**
     * Send a shell.code_execution message, return response msg. The response does not carry computation result, only carries status
     */
    override fun send(message: JPMessage<Shell.ExecuteRequest.Out.MetaData, Shell.ExecuteRequest.Out.Content>): Optional<ResponseZ> {
        val socketResult: Optional<ZMsg> = zmqMsgSender.send(message)
        val rt:Optional<List<String>> = socketResult.map { msg ->
            val strBuilder:List<String> = msg.map { frame->frame.getString(Charsets.UTF_8) }
            strBuilder
        }
        val rt2 = rt.map {
            JPMessage.fromPayload<Shell.ExecuteRequest.In.MetaData, Shell.ExecuteRequest.In.Content>(it)
        }
        return rt2
    }
}
