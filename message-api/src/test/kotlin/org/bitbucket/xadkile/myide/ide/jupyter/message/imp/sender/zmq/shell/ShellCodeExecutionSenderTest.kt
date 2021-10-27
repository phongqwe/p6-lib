package org.bitbucket.xadkile.myide.ide.jupyter.message.imp.sender.zmq.shell

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.channel.ChannelInfo
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgType
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.shell.execute.ShellCodeExecutionContent
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.utils.MsgCounterImp
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.utils.SequentialMsgIdGenerator
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.session.Session
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.TestInstance
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import org.zeromq.ZThread
import test.utils.TestOnJupyter

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ShellCodeExecutionSenderTest : TestOnJupyter(){
    class ZZZ(val subSocket: ZMQ.Socket) : ZThread.IDetachedRunnable {
        override fun run(args: Array<out Any>?) {
            while (true) {
                val o = subSocket.recvStr()
                println(o)
            }
        }
    }
    @Test
    fun send() {
        val context = ZContext()
        val connectionFile = this.connectionFileContent
        val session = Session.autoCreate(connectionFile.key)
        val channelInfo = ChannelInfo("Shell", "tcp", connectionFile.ip, connectionFile.controlPort)
        val subCHannel = ChannelInfo("Shell", "tcp", connectionFile.ip, connectionFile.iopubPort)

        val subSocket = context.createSocket(SocketType.SUB)
        subSocket.connect(subCHannel.makeAddress())
        println(subCHannel.makeAddress())
        subSocket.subscribe("")

        val runnable = ZZZ(subSocket)
        ZThread.start(runnable)
        val sender = ShellCodeExecutionSender(
            zmqContext = context,
            session = session,
            address = channelInfo.makeAddress(),
            msgIdGenerator = SequentialMsgIdGenerator(session.sessionId, MsgCounterImp())
        )

        val input = ShellCodeExecutionContent(
            code = "x=1+1*2;y=x*2;y",
            silent = false,
            storeHistory = true,
            userExpressions = mapOf(),
            allowStdin = false,
            stopOnError = true
        )
//        val out:String = sender.send(MsgType.Shell.execute_request, input)
//        if(out.isSuccess){
            println("==OUT==\n${sender.send(MsgType.Shell.execute_request, input)}\n====")
//        }

        Thread.sleep(10000)

        context.close()
    }
}
