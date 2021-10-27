package org.bitbucket.xadkile.myide.ide.jupyter.message.imp.sender.zmq

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgType
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.shell.execute.ShellCodeExecutionContent
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.sender.MsgSender
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.session.Session
import org.zeromq.SocketType
import org.zeromq.ZContext

class ShellCodeExecutionSender(val zmqContext: ZContext, val session:Session, val address:String) : MsgSender<ShellCodeExecutionContent,String>{
    private val zmqMsgSender = ZMQMsgSender<ShellCodeExecutionContent>(
        socket = zmqContext.let {
            val socket = it.createSocket(SocketType.REQ)
            socket.connect(address)
            socket
        },
        session=session
    )

    override fun send(msgType: MsgType, msgContent: ShellCodeExecutionContent): String {
        val sock = zmqMsgSender.send(msgType,msgContent)
        val strBuilder = StringBuilder()
        val first  = sock.recvStr()

        if(first!=null && first.isNotEmpty()) strBuilder.appendLine((first))
        while(sock.hasReceiveMore()){
            val n = sock.recvStr()
            if(n!=null && n.isNotEmpty()) strBuilder.appendLine((n))
        }
        val reply = strBuilder.toString()
        return reply
    }
}
