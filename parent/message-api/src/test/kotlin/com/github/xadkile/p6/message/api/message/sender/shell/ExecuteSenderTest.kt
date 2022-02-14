package com.github.xadkile.p6.message.api.message.sender.shell

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.unwrap
import com.github.xadkile.p6.message.api.message.protocol.MsgStatus
import com.github.xadkile.p6.message.api.message.protocol.data_interface_definition.Shell
import com.github.xadkile.p6.test.utils.TestOnJupyter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ExecuteSenderTest : TestOnJupyter() {

    @BeforeEach
    fun beforeEach() {
        runBlocking {
            kernelContext.startAll()
        }
    }

    val message: ExecuteRequest = ExecuteRequest.autoCreate(
        sessionId = "session_id",
        username = "user_name",
        msgType = Shell.Execute.Request.msgType,
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
    val malformedCodeMsg: ExecuteRequest = ExecuteRequest.autoCreate(
        sessionId = "session_id",
        username = "user_name",
        msgType = Shell.Execute.Request.msgType,
        msgContent = Shell.Execute.Request.Content(
            code = "x=1+1*2;functionx()",
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
        runBlocking {
            val sender2 = ExecuteSender(
                kernelContext
            )

            val out = sender2.send(message, Dispatchers.Default)

            assertTrue { out is Ok }
            assertEquals(MsgStatus.ok, out.unwrap().content.status)
            println("==OUT==\n${out.unwrap()}\n====")
        }

    }

    /**
     * send malformed python code
     */
    @Test
    fun send_malformedCode() {
        runBlocking {
            val sender2 = ExecuteSender(
                kernelContext
            )

            val out = sender2.send(malformedCodeMsg, Dispatchers.Default)

            assertTrue { out is Ok }
            assertEquals(MsgStatus.error, out.unwrap().content.status)
            println("==OUT==\n${out.unwrap()}\n====")
        }

    }

    /**
     * unable to send message, because kernel stopped
     */
    @Test
    fun send_fail() {
        runBlocking {
            val sender2 = ExecuteSender(kernelContext)
            kernelContext.stopAll()
            val out = sender2.send(message, Dispatchers.Default)
            assertTrue(out is Err, out.toString())
            println(out)
        }
    }
}
