package org.bitbucket.xadkile.myide.ide.jupyter.message.imp.shell

import com.github.michaelbull.result.unwrap
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.channel.ChannelInfo
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.JPRawMessage
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.data_definition.Shell
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.session.Session
import org.bitbucket.xadkile.myide.ide.jupyter.message.di.DaggerMessageApiComponent
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.TestInstance
import org.zeromq.*
import test.utils.TestOnJupyter

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ShellCodeExecutionSenderTest : TestOnJupyter(){
    class ZListener(val subSocket: ZMQ.Socket) : ZThread.IDetachedRunnable {
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

        val shellChannel = ChannelInfo("Shell", "tcp", connectionFile.ip, connectionFile.shellPort)
        val iopubChannel = ChannelInfo("IOPub", "tcp", connectionFile.ip, connectionFile.iopubPort)

        val subSocket = context.createSocket(SocketType.SUB)
        subSocket.connect(iopubChannel.makeAddress())
        subSocket.subscribe("")

        val runnable = ZListener(subSocket)
        ZThread.start(runnable)

        val msgApiComponent = DaggerMessageApiComponent
            .builder()
            .session(session)
            .shellChannel(shellChannel)
            .zContext(context)
            .build()

         val sender2:ExecuteRequestSender = msgApiComponent.shellExecuteRequestSender()

        val input = Shell.ExecuteRequest.Out.Content(
            code = "x=1+1*2;y=x*2;y",
            silent = false,
            storeHistory = true,
            userExpressions = mapOf(),
            allowStdin = false,
            stopOnError = true
        )
        val out = sender2.send(Shell.ExecuteRequest.msgType, input)
        if(out.isPresent()){
            println("==OUT==\n${out.get()}\n====")
        }
        Thread.sleep(10000)
        context.close()
    }
}
