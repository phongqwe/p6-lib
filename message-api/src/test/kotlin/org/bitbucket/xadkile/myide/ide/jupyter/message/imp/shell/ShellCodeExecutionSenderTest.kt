package org.bitbucket.xadkile.myide.ide.jupyter.message.imp.shell

import com.github.michaelbull.result.unwrap
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.channel.ChannelInfo
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.JPRawMessage
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.data_definition.Shell
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
                val z = JPRawMessage.fromRecvPayload(msgL.map{it.toByteArray(Charsets.UTF_8)}).unwrap()
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
        val sender = ExecuteRequestSender(
            zmqContext = context,
            session = session,
            address = channelInfo.makeAddress(),
            msgIdGenerator = SequentialMsgIdGenerator(session.sessionId, MsgCounterImp())
        )

        val input = Shell.ExecuteRequest.Out.Content(
            code = "x=1+1*2;y=x*2;y",
            silent = false,
            storeHistory = true,
            userExpressions = mapOf(),
            allowStdin = false,
            stopOnError = true
        )
        val out = sender.send(Shell.ExecuteRequest.msgType, input)
        if(out.isPresent()){
            println("==OUT==\n${out.get()}\n====")
        }
        Thread.sleep(10000)
        context.close()
    }
}
