package com.github.xadkile.bicp.message.api.msg.sender.composite

import com.github.michaelbull.result.*
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelContextReadOnly
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelIsDownException
import com.github.xadkile.bicp.message.api.msg.listener.IOPubListener
import com.github.xadkile.bicp.message.api.msg.listener.MsgListener
import com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition.Shell
import com.github.xadkile.bicp.message.api.msg.sender.MsgSender
import com.github.xadkile.bicp.message.api.msg.sender.exception.UnableToSendMsgException
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteReply
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteRequest
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteSender
import com.github.xadkile.bicp.test.utils.TestOnJupyter
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.lang.IllegalStateException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CodeExecutionSenderTest : TestOnJupyter() {


    @AfterEach
    fun ae(){
        kernelContext.stopKernel()
    }
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

            val sender = CodeExecutionSender(
                kernelContext = kernelContext.conv(),
                executeSender = ExecuteSender(kernelContext.conv()),
                ioPubListener = IOPubListener(
                    kernelContext = kernelContext.conv(),
                    defaultHandler = { msg, l ->
                        println(msg)
                    },
                    parseExceptionHandler = { e, l ->
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

            // p: mockk is horribly slow here
            val mockSender = object : MsgSender<ExecuteRequest, Result<ExecuteReply, Exception>> {
                override fun getKernelContext(): KernelContextReadOnly {
                    TODO("Not yet implemented")
                }

                override suspend fun send(
                    message: ExecuteRequest,
                    dispatcher: CoroutineDispatcher,
                ): Result<ExecuteReply, Exception> {
                    return Err(UnableToSendMsgException(message))
                }
            }

            val sender = CodeExecutionSender(kernelContext.conv(), mockSender, IOPubListener(kernelContext))
            val o = sender.send(message, Dispatchers.Default)
            assertTrue(o is Err, o.toString())
            assertTrue(o.unwrapError() is UnableToSendMsgException)
            assertEquals(message, (o.unwrapError() as UnableToSendMsgException).msg)
        }
    }

    @Test
    fun send_kernelNotRunning()= runBlocking {
        kernelContext.stopKernel()
        val sender = CodeExecutionSender(kernelContext.conv(),ExecuteSender(kernelContext.conv()),IOPubListener(kernelContext))
        val o = sender.send(message)
        assertTrue(o is Err)
        assertTrue((o.unwrapError()) is KernelIsDownException)
    }

    @Test
    fun send_unableToStartListener() = runBlocking{
        kernelContext.startKernel()
        val mockListener = mockk<MsgListener>().also {
            coEvery{it.start(any(),any())} returns Err(IllegalStateException())
            every { it.addHandler(any()) } returns Unit
            coEvery { it.stop() } returns Unit
        }
        val sender = CodeExecutionSender(kernelContext.conv(),ExecuteSender(kernelContext.conv()),mockListener)
        val o = sender.send(message)
        assertTrue(o is Err)
        assertTrue((o.unwrapError()) is IllegalStateException)
    }
}
