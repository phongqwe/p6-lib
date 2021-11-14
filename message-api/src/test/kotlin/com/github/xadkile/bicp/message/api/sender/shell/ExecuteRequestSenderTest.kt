package com.github.xadkile.bicp.message.api.sender.shell

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.unwrap
import com.github.xadkile.bicp.message.api.protocol.message.JPRawMessage
import com.github.xadkile.bicp.message.api.protocol.message.data_interface_definition.Shell
import com.github.xadkile.bicp.message.api.sender.ZMQMsgSender
import com.github.xadkile.bicp.test.utils.TestOnJupyter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import org.zeromq.ZThread

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ExecuteRequestSenderTest : TestOnJupyter() {
    class ZListener(val subSocket: ZMQ.Socket) : ZThread.IDetachedRunnable {
        override fun run(args: Array<out Any>?) {
            val msgL = mutableListOf<String>()
            while (true) {
                val o = subSocket.recvStr()
                msgL.add(o)
                while (subSocket.hasReceiveMore()) {
                    val m = subSocket.recvStr()
                    msgL.add(m)
                }
                val z = JPRawMessage.fromRecvPayload(msgL.map { it.toByteArray(Charsets.UTF_8) }).unwrap()
                println(z)
                msgL.clear()
            }
        }
    }

    @Test
    fun send() {

        val context = ZContext()
        val connectionFile = this.ipythonContext.getConnectionFileContent().unwrap()
        val session = this.ipythonContext.getSession().unwrap()
        val ioPubSocket: ZMQ.Socket = context.createSocket(SocketType.SUB)
        ioPubSocket.connect(connectionFile.createIOPubChannel().makeAddress())
        ioPubSocket.subscribe("")
        val runnable = ZListener(ioPubSocket)
        ZThread.start(runnable)

        val shellSocket = context.createSocket(SocketType.REQ).also {
            it.connect(connectionFile.createShellChannel().makeAddress())
        }
        val sender2 = ExecuteRequestSender(
            shellSocket,
            this.ipythonContext.getMsgEncoder().unwrap(),
        )

        val message:ExecuteRequestInputMessage = ExecuteRequestInputMessage.autoCreate(
            sessionId = session.getSessionId(),
            username = session.getUserName(),
            msgType = Shell.ExecuteRequest.msgType,
            msgContent = Shell.ExecuteRequest.Input.Content(
                code = "x=1+1*2;y=x*2;y",
                silent = false,
                storeHistory = true,
                userExpressions = mapOf(),
                allowStdin = false,
                stopOnError = true
            ),
            "1235plm"
        )
        val out = sender2.send(
            message
        )
        if (out is Ok) {
            println("==OUT==\n${out.unwrap()}\n====")
        }
        Thread.sleep(10000)
        context.close()
    }
}
