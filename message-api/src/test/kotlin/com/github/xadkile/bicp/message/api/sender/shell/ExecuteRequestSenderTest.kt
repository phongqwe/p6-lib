package com.github.xadkile.bicp.message.api.sender.shell

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.unwrap
import com.github.xadkile.bicp.message.api.protocol.message.JPRawMessage
import com.github.xadkile.bicp.message.api.protocol.message.MsgStatus
import com.github.xadkile.bicp.message.api.protocol.message.data_interface_definition.Shell
import com.github.xadkile.bicp.test.utils.TestOnJupyter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import org.zeromq.ZThread
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
                val z = JPRawMessage.fromPayload(msgL.map { it.toByteArray(Charsets.UTF_8) }).unwrap()
                println(z)
                msgL.clear()
            }
        }
    }

    val message: ExecuteRequestInput = ExecuteRequestInput.autoCreate(
        sessionId = "session_id",
        username = "user_name",
        msgType = Shell.ExecuteRequest.msgType,
        msgContent = Shell.ExecuteRequest.Input.Content(
            code = "x=1+1*2;y=x*2;y",
            silent = false,
            storeHistory = true,
            userExpressions = mapOf(),
            allowStdin = false,
            stopOnError = true
        ),
        "msg_id_abc_123"
    )
    val malformedCodeMsg: ExecuteRequestInput = ExecuteRequestInput.autoCreate(
        sessionId = "session_id",
        username = "user_name",
        msgType = Shell.ExecuteRequest.msgType,
        msgContent = Shell.ExecuteRequest.Input.Content(
            code = "x=1+1*2;abc",
            silent = false,
            storeHistory = true,
            userExpressions = mapOf(),
            allowStdin = false,
            stopOnError = true
        ),
        "msg_id_abc_123"
    )

    @Test
    fun send_ok() {
        val context = ZContext()
        context.use {
            val connectionFile = this.ipythonContext.getConnectionFileContent().unwrap()
//            val ioPubSocket: ZMQ.Socket = context.createSocket(SocketType.SUB)
//            ioPubSocket.connect(connectionFile.createIOPubChannel().makeAddress())
//            ioPubSocket.subscribe("")
//            val runnable = ZListener(ioPubSocket)
//            ZThread.start(runnable)

            val shellSocket = context.createSocket(SocketType.REQ).also {
                it.connect(connectionFile.createShellChannel().makeAddress())
            }

            val sender2 = ExecuteRequestSender(
                shellSocket,
                this.ipythonContext.getMsgEncoder().unwrap(),
            )

            val out = sender2.send(message)

            assertTrue { out is Ok }
            assertEquals(MsgStatus.ok, out.unwrap().content.status)
            println("==OUT==\n${out.unwrap()}\n====")
        }
    }

    @Test
    fun send_malformedCode() {
        val context = ZContext()
        context.use {
            val connectionFile = this.ipythonContext.getConnectionFileContent().unwrap()

            val shellSocket = context.createSocket(SocketType.REQ).also {
                it.connect(connectionFile.createShellChannel().makeAddress())
            }

            val sender2 = ExecuteRequestSender(
                shellSocket,
                this.ipythonContext.getMsgEncoder().unwrap(),
            )

            val out = sender2.send(malformedCodeMsg)

            assertTrue { out is Ok }
            assertEquals(MsgStatus.error, out.unwrap().content.status)
            println("==OUT==\n${out.unwrap()}\n====")
        }
    }

    @Test
    fun send_fail() {
        val context = ZContext()
        context.use {
            val channelProvider =  this.ipythonContext.getChannelProvider().unwrap()
            val shellSocket = context.createSocket(SocketType.REQ).also {
                it.connect(channelProvider.getShellChannel().makeAddress())
            }
            val sender2 = ExecuteRequestSender(
                shellSocket,
                this.ipythonContext.getMsgEncoder().unwrap(),
            )
            this.ipythonContext.stopIPython()
            val out = sender2.send(
                message
            )
            assertTrue { out is Err }
//            assertEquals(MsgStatus.ok, out.unwrap().content.status)
//            println("==OUT==\n${out.unwrap()}\n====")
        }
    }
}
