package org.bitbucket.xadkile.myide.ide.jupyter.message.imp.shell

import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.channel.IsShellAddress
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.channel.IsShellChannel
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgType
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.data_definition.Shell
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.utils.MsgIdGenerator
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.sender.MsgSender
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.session.Session
import org.bitbucket.xadkile.myide.ide.jupyter.message.imp.ZMQMsgSender
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class ExecuteRequestSender @Inject constructor(
    private val zmqContext: ZContext,
    private val session: Session,
    @IsShellAddress
    private val address: String,
    @Named("sequential")
    private val msgIdGenerator: MsgIdGenerator
) : MsgSender<Shell.ExecuteRequest.Out.Content, Optional<String>> {

    private val zmqMsgSender =
        ZMQMsgSender<Shell.ExecuteRequest.Out.Content>(
            socket = zmqContext.createSocket(SocketType.REQ).also {
                it.connect(address)
            },
            session = session,
            msgIdGenerator = msgIdGenerator
        )

    /**
     * Send a shell.code_execution message, return response msg. The response does not carry computation result, only carries status
     */
    override fun send(msgType: MsgType, msgContent: Shell.ExecuteRequest.Out.Content): Optional<String> {
        val sockResult: Optional<ZMQ.Socket> = zmqMsgSender.send(msgType, msgContent)
        val rt:Optional<String> = sockResult.map { sock ->
            val strBuilder = StringBuilder()
            val first:String? = sock.recvStr()
            if (first != null && first.isNotEmpty()) strBuilder.appendLine((first))
            while (sock.hasReceiveMore()) {
                val n:String? = sock.recvStr()
                if (n != null && n.isNotEmpty()) strBuilder.appendLine((n))
            }
            val reply = strBuilder.toString()
            reply
        }
        return rt
    }
}
