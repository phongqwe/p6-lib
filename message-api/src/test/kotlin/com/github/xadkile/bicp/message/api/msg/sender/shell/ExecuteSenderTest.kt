package com.github.xadkile.bicp.message.api.msg.sender.shell

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.unwrap
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgStatus
import com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition.Shell
import com.github.xadkile.bicp.test.utils.TestOnJupyter
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.zeromq.SocketType
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ExecuteSenderTest : TestOnJupyter() {

    @BeforeEach
    fun beforeEach(){
        this.ipythonContext.startIPython()
    }

    val message: ExecuteReply = ExecuteRequest.autoCreate(
        sessionId = "session_id",
        username = "user_name",
        msgType = Shell.Execute.msgType,
        msgContent = Shell.Execute.Request.Content(
            code = "x=1+1*2;y=x*2;y",
            silent = false,
            storeHistory = true,
            userExpressions = mapOf(),
            allowStdin = false,
            stopOnError = true
        ),
        "msg_id_abc_123"
    )
    val malformedCodeMsg: ExecuteReply = ExecuteRequest.autoCreate(
        sessionId = "session_id",
        username = "user_name",
        msgType = Shell.Execute.msgType,
        msgContent = Shell.Execute.Request.Content(
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

        val zcontext = this.ipythonContext.zContext()
        val shellSocket = zcontext.createSocket(SocketType.REQ).also {
            it.connect(this.iPythonContextConv.getShellAddress().unwrap())
        }

        val sender2 = ExecuteSender(
            shellSocket,
            this.ipythonContext.getMsgEncoder().unwrap(),
            this.ipythonContext.getHeartBeatService().unwrap().conv(),
            this.ipythonContext.zContext()
        )

        val out = sender2.send(message)

        assertTrue { out is Ok }
        assertEquals(MsgStatus.ok, out.unwrap().content.status)
        println("==OUT==\n${out.unwrap()}\n====")
    }

    /**
     * send malformed python code
     */
    @Test
    fun send_malformedCode() {
        val connectionFile = this.ipythonContext.getConnectionFileContent().unwrap()
        val zcontext = this.ipythonContext.zContext()
        val shellSocket = zcontext.createSocket(SocketType.REQ).also {
            it.connect(connectionFile.createShellChannel().makeAddress())
        }

        val sender2 = ExecuteSender(
            shellSocket,
            this.ipythonContext.getMsgEncoder().unwrap(),
            this.ipythonContext.getHeartBeatService().unwrap().conv(),
            this.ipythonContext.zContext()
        )

        val out = sender2.send(malformedCodeMsg)

        assertTrue { out is Ok }
        assertEquals(MsgStatus.error, out.unwrap().content.status)
        println("==OUT==\n${out.unwrap()}\n====")
    }

    /**
     * unable to send message
     */
    @Test
    fun send_fail() {
        val zcontext = this.ipythonContext.zContext()
        val channelProvider = this.ipythonContext.getChannelProvider().unwrap()
        val shellSocket = zcontext.createSocket(SocketType.REQ).also {
            it.connect(channelProvider.shellChannel().makeAddress())
        }
        val sender2 = ExecuteSender(
            shellSocket,
            this.ipythonContext.getMsgEncoder().unwrap(),
            this.ipythonContext.getHeartBeatService().unwrap().conv(),
            this.ipythonContext.zContext()
        )
        this.ipythonContext.stopIPython()
        val out = sender2.send(
            message
        )
        assertTrue (out is Err,out.toString())
    }
}
