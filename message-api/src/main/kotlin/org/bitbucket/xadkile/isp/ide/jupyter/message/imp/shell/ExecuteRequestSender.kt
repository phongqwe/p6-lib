package org.bitbucket.xadkile.isp.ide.jupyter.message.imp.shell

import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message.JPMessage
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message.MsgType
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message.data_definition.Shell
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.other.MsgIdGenerator
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.other.ProtocolUtils
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.sender.MsgSender
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.connection.SessionInfo
import org.bitbucket.xadkile.isp.ide.jupyter.message.imp.ZMQMsgSender
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.util.*
import javax.inject.Inject
import javax.inject.Named

typealias ResponseZ = JPMessage<Shell.ExecuteRequest.In.MetaData, Shell.ExecuteRequest.In.Content>

class ExecuteRequestSender @Inject constructor(
    private val zmqContext: ZContext,
    private val sessionInfo: SessionInfo,
    @Shell.Address
    private val address: String,
    @Named("sequential")
    private val msgIdGenerator: MsgIdGenerator
) : MsgSender<Shell.ExecuteRequest.Out.Content,
        Optional<ResponseZ>> {

    private val zmqMsgSender =
        ZMQMsgSender<Shell.ExecuteRequest.Out.Content>(
            socket = zmqContext.createSocket(SocketType.REQ).also {
                it.connect(address)
            },
            sessionInfo = sessionInfo,
            msgIdGenerator = msgIdGenerator
        )

    /**
     * Send a shell.code_execution message, return response msg. The response does not carry computation result, only carries status
     */
    override fun send(msgType: MsgType, msgContent: Shell.ExecuteRequest.Out.Content): Optional<ResponseZ> {
        val socketResult: Optional<ZMQ.Socket> = zmqMsgSender.send(msgType, msgContent)
        val rt:Optional<List<String>> = socketResult.map { socket ->
            val strBuilder = mutableListOf<String>()
            val first:String? = socket.recvStr()
            if (first != null && first.isNotEmpty()) strBuilder.add((first))
            while (socket.hasReceiveMore()) {
                val n:String? = socket.recvStr()
                if (n != null && n.isNotEmpty()) strBuilder.add((n))
            }
            strBuilder
        }
        val rt2 = rt.map { str->
            ProtocolUtils.msgGson.fromJson(str[5],Shell.ExecuteRequest.In.Content::class.java)
        }
        TODO()
//        return rt2
    }
}
