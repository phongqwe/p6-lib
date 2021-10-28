package org.bitbucket.xadkile.myide.ide.jupyter.message.imp.shell

import arrow.core.Option
import arrow.core.getOrElse
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgType
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.shell.execute.ShellCodeExecutionContent
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.utils.MsgIdGenerator
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.sender.MsgSender
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.session.Session
import org.bitbucket.xadkile.myide.ide.jupyter.message.imp.ZMQMsgSender
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ

class ShellCodeExecutionSender(
    private val zmqContext: ZContext,
    private val session: Session,
    private val address: String,
    val msgIdGenerator: MsgIdGenerator
) : MsgSender<ShellCodeExecutionContent, Option<String>> {

    private val zmqMsgSender =
        ZMQMsgSender<ShellCodeExecutionContent>(
            socket = zmqContext.createSocket(SocketType.REQ).also {
                it.connect(address)
            },
            session = session,
            msgIdGenerator = msgIdGenerator
        )

    //    override fun send(msgType: MsgType, msgContent: ShellCodeExecutionContent): Either<CantSendMsgException,String> {
    override fun send(msgType: MsgType, msgContent: ShellCodeExecutionContent): Option<String> {
//        val sockResult: Either<CantSendMsgException, ZMQ.Socket> = zmqMsgSender.send(msgType, msgContent)
        val sockResult: Option<ZMQ.Socket> = zmqMsgSender.send(msgType, msgContent)
        val rt = sockResult.map { sock ->
            val strBuilder = StringBuilder()
            val first = sock.recvStr()
            if (first != null && first.isNotEmpty()) strBuilder.appendLine((first))
            while (sock.hasReceiveMore()) {
                val n = sock.recvStr()
                if (n != null && n.isNotEmpty()) strBuilder.appendLine((n))
            }
            val reply = strBuilder.toString()
            reply
        }
        return rt
    }
}
