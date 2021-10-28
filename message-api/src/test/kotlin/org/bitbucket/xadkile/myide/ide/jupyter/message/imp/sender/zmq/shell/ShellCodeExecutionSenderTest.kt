package org.bitbucket.xadkile.myide.ide.jupyter.message.imp.sender.zmq.shell

import arrow.core.computations.ResultEffect.bind
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.channel.ChannelInfo
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.InRequestFacade
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgType
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.shell.execute.ShellCodeExecutionContent
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.utils.MsgCounterImp
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.utils.SequentialMsgIdGenerator
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.session.Session
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.TestInstance
import org.zeromq.*
import test.utils.TestOnJupyter

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ShellCodeExecutionSenderTest : TestOnJupyter(){
    class ZZZ(val subSocket: ZMQ.Socket, val session: Session) : ZThread.IDetachedRunnable {
        override fun run(args: Array<out Any>?) {
            val msgL = mutableListOf<String>()
            while (true) {
                val o = subSocket.recvStr()
                msgL.add(o)
                while(subSocket.hasReceiveMore()){
                    val m = subSocket.recvStr()
                    msgL.add(m)
                }
                val z = InRequestFacade.fromRecvPayload(msgL.map{it.toByteArray(Charsets.UTF_8)}).bind()
                println(z)
                msgL.clear()
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

        val runnable = ZZZ(subSocket,session)
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
        val out = sender.send(MsgType.Shell.execute_request, input)
        if(out.isDefined()){
            println("==OUT==\n${out.orNull()}\n====")
        }
        Thread.sleep(10000)
        context.close()
    }
}
