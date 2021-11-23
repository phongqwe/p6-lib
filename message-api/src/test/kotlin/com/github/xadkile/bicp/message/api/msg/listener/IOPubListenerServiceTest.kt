package com.github.xadkile.bicp.message.api.msg.listener

import com.github.michaelbull.result.unwrap
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteRequestInput
import com.github.xadkile.bicp.message.api.protocol.message.data_interface_definition.Shell
import com.github.xadkile.bicp.test.utils.TestOnJupyter
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class IOPubListenerServiceTest : TestOnJupyter() {

    @Test
    fun start() {
        runBlocking{
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

            ipythonContext.startIPython()

            val listener = IOPubListenerService(ipythonContext.getSocketProvider().unwrap())
            listener.start()
            val sender = ipythonContext.getSenderProvider().unwrap().getExecuteRequestSender()
            val o = sender.send(message)
            delay(1000)
            listener.stop()
        }
    }

    @Test
    fun stop() {
    }
}
