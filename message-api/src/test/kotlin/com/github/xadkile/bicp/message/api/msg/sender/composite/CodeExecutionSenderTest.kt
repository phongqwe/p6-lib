package com.github.xadkile.bicp.message.api.msg.sender.composite

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.unwrap
import com.github.xadkile.bicp.message.api.msg.listener.IOPubListener
import com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition.Shell
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteRequest
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteSender
import com.github.xadkile.bicp.test.utils.TestOnJupyter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.TestInstance
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CodeExecutionSenderTest : TestOnJupyter() {

    /**
     * TODO write more edge case test:
     *  - unable to send message
     *  - pub not arrived, what to do?
     */
    @Test
    fun send_Ok() {
        runBlocking {
            kernelContext.startKernel()
            kernelContext.getHeartBeatService().unwrap().start()
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
            val sender = CodeExecutionSender(
                kernelContext = kernelContext.conv(),
                executeSender = ExecuteSender(kernelContext.conv()),
                ioPubListener = IOPubListener(
                    kernelContext = kernelContext.conv(),
                    defaultHandler = { msg,l ->
                        println(msg)
                    },
                    parseExceptionHandler = { e,l ->
                        println(e)
                    }
                )
            )
            val o = sender.send(message, Dispatchers.Default)
            assertTrue(o is Ok, o.toString())
        }
    }

    /**
     * When unable to send a message, the composite sender should return an Err indicate such condition
     */
    @Test
    fun send_fail() {
        runBlocking {
            kernelContext.startKernel()
            kernelContext.getHeartBeatService().unwrap().start()

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
//            val sender = CodeExecutionSender(kernelContext.conv())
//            val o = sender.send(message, Dispatchers.Default)
//            assertTrue(o is Ok, o.toString())
        }
    }
}
