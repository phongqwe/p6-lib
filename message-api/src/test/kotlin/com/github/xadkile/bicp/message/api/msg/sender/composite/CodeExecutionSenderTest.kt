package com.github.xadkile.bicp.message.api.msg.sender.composite

import com.github.michaelbull.result.unwrap
import com.github.xadkile.bicp.message.api.msg.listener.IOPubListener
import com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition.Shell
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteRequest
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteSender
import com.github.xadkile.bicp.test.utils.TestOnJupyter
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CodeExecutionSenderTest : TestOnJupyter(){

    @Test
    fun send() {
        ipythonContext.startIPython()
        ipythonContext.getHeartBeatService().unwrap().start()
        runBlocking {

            val message: ExecuteRequest = ExecuteRequest.autoCreate(
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
            val sender = CodeExecutionSender(ipythonContext.conv())
            val o = sender.send(message)
            println(o)
        }
    }
}
