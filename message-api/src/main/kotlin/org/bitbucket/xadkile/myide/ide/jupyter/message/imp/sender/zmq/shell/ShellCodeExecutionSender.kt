package org.bitbucket.xadkile.myide.ide.jupyter.message.imp.sender.zmq.shell

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgType
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.shell.execute.ShellCodeExecutionContent
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.utils.MsgIdGenerator
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.sender.MsgSender
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.session.Session
import org.bitbucket.xadkile.myide.ide.jupyter.message.imp.sender.zmq.ZMQMsgSender
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ

class ShellCodeExecutionSender(
    private val zmqContext: ZContext,
    private val session: Session,
    private val address: String,
    val msgIdGenerator: MsgIdGenerator
) : MsgSender<ShellCodeExecutionContent, String?> {

    private val zmqMsgSender =
        ZMQMsgSender<ShellCodeExecutionContent>(
            socket = zmqContext.createSocket(SocketType.REQ).also {
                it.connect(address)
            },
            session = session,
            msgIdGenerator = msgIdGenerator
        )

    override fun send(msgType: MsgType, msgContent: ShellCodeExecutionContent): String? {
        val sockResult: ZMQ.Socket = zmqMsgSender.send(msgType, msgContent)
//        if (sockResult.isSuccess) {
            val sock = sockResult
            val strBuilder = StringBuilder()
            val first = sock.recvStr()
            if (first != null && first.isNotEmpty()) strBuilder.appendLine((first))
            while (sock.hasReceiveMore()) {
                val n = sock.recvStr()
                if (n != null && n.isNotEmpty()) strBuilder.appendLine((n))
            }
            val reply = strBuilder.toString()
            return reply
//            return Result.success(reply)
//        } else {
////            val exception = sockResult.exceptionOrNull() ?: Exception("Error")
////            return Result.failure(exception)
//            return null
//        }
    }
}
