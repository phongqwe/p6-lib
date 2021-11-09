package com.github.xadkile.bicp.message.api.sender

import com.github.michaelbull.result.unwrap
import com.github.xadkile.bicp.message.api.protocol.message.JPRawMessage
import com.github.xadkile.bicp.message.api.protocol.message.data_interface_definition.Shell
import com.github.xadkile.bicp.message.api.connection.SessionInfo
import com.github.xadkile.bicp.message.di.DaggerMessageApiComponent
import com.github.xadkile.bicp.message.api.sender.shell.ExecuteRequestSender
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.TestInstance
import org.zeromq.*
import com.github.xadkile.bicp.test.utils.TestOnJupyter

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
        val session = SessionInfo.autoCreate(connectionFile.key)

        val msgApiComponent = DaggerMessageApiComponent
            .builder()
            .session(session)
            .connectionFile(connectionFile)
            .zContext(context)
            .build()

        val iopubChannel = msgApiComponent.iopubChannel()
        val ioPubSocket = context.createSocket(SocketType.SUB)
        ioPubSocket.connect(iopubChannel.makeAddress())
        ioPubSocket.subscribe("")
        val runnable = ZListener(ioPubSocket)
        ZThread.start(runnable)

         val sender2: ExecuteRequestSender = msgApiComponent.shellExecuteRequestSender()

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
