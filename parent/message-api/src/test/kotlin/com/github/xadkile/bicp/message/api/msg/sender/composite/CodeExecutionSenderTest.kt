package com.github.xadkile.bicp.message.api.msg.sender.composite

import com.github.michaelbull.result.*
import com.github.xadkile.bicp.message.api.connection.kernel_context.exception.KernelIsDownException
import com.github.xadkile.bicp.message.api.connection.service.iopub.IOPubListenerService
import com.github.xadkile.bicp.message.api.connection.service.iopub.IOPubListenerServiceImpl
import com.github.xadkile.bicp.message.api.msg.listener.HandlerContainerImp
import com.github.xadkile.bicp.message.api.msg.listener.IOPubListener
import com.github.xadkile.bicp.message.api.msg.listener.exception.IOPubListenerNotRunningException
import com.github.xadkile.bicp.message.api.msg.protocol.data_interface_definition.Shell
import com.github.xadkile.bicp.message.api.msg.sender.MsgSender
import com.github.xadkile.bicp.message.api.msg.sender.exception.UnableToSendMsgException
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteReply
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteRequest
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteSender
import com.github.xadkile.bicp.message.api.other.Sleeper
import com.github.xadkile.bicp.test.utils.TestOnJupyter
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CodeExecutionSenderTest : TestOnJupyter() {

    lateinit var ioPubService: IOPubListenerServiceImpl

    @AfterEach
    fun ae() {
        ioPubService.stop()
        kernelContext.stopKernel()
    }

    @BeforeEach
    fun beforeEach() {
        kernelContext.startKernel()
        ioPubService = IOPubListenerServiceImpl(
            IOPubListener(
                kernelContext = kernelContext.conv(),
                defaultHandler = { msg, l ->
                    println(msg)
                },
                parseExceptionHandler = { e, l ->
                    println(e)
                },
                parallelHandler = true,
                handlerContainer = HandlerContainerImp()
            ),
            cScope = GlobalScope
        )
        ioPubService.start()
    }

    val message: ExecuteRequest = ExecuteRequest.autoCreate(
        sessionId = "session_id",
        username = "user_name",
        msgType = Shell.Execute.Request.msgType,
        msgContent = Shell.Execute.Request.Content(
            code ="""
                x=0
                y=0
                for k in range(1000):
                    x=k+1
                    y=x*2
                y
            """.trimIndent(),
            silent = false,
            storeHistory = true,
            userExpressions = mapOf(),
            allowStdin = false,
            stopOnError = true
        ),
        "msg_id_abc_123"
    )

    /**
     * See if it is feasible to bombard kernel with many request at the same time
     */
    @Test
    fun stressTest() {
        val okCount = AtomicInteger(0)
        // ph: send 1000 messages
        val msgCount = 1000
        runBlocking {
            val time = measureTimeMillis {

                for (x in 0 until msgCount) {
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
                        kernelContext.getMsgIdGenerator().unwrap().next()
                    )

                    val sender = CodeExecutionSender(
                        kernelContext = kernelContext.conv(),
                        executeSender = ExecuteSender(kernelContext.conv()),
                        ioPubListenerService = ioPubService
                    )

                    val o = sender.send(message, Dispatchers.IO)
                    assertTrue(o is Ok, o.toString())
                    okCount.incrementAndGet()
                }
            }
            println("Sending $msgCount messages take ${time / 1000} seconds")
        }
        assertEquals(msgCount, okCount.get())
    }

    @Test
    fun send_Ok() {
        runBlocking {
            val sender = CodeExecutionSender(
                kernelContext = kernelContext.conv(),
                executeSender = ExecuteSender(kernelContext.conv()),
                ioPubListenerService = ioPubService
            )
            val o = sender.send(message, Dispatchers.Default)
            kotlin.test.assertTrue(o is Ok, o.toString())
            println(o.value.content)
        }
    }

    /**
     * When unable to send a message, the composite sender should return an Err indicate such condition
     */
    @Test
    fun send_fail() {
        runBlocking {
            kernelContext.startKernel()

            // ph: mockk is horribly slow here
            val mockSender = object : MsgSender<ExecuteRequest, Result<ExecuteReply, Exception>> {
                override suspend fun send(
                    message: ExecuteRequest,
                    dispatcher: CoroutineDispatcher,
                ): Result<ExecuteReply, Exception> {
                    return Err(UnableToSendMsgException(message))
                }
            }

            val sender = CodeExecutionSender(kernelContext.conv(), mockSender, ioPubService)
            val o = sender.send(message, Dispatchers.Default)
            kotlin.test.assertTrue(o is Err, o.toString())
            kotlin.test.assertTrue(o.unwrapError() is UnableToSendMsgException)
            kotlin.test.assertEquals(message, (o.unwrapError() as UnableToSendMsgException).msg)
        }
    }

    @Test
    fun send_kernelNotRunning() = runBlocking {
        kernelContext.stopKernel()
        val sender =
            CodeExecutionSender(kernelContext.conv(), ExecuteSender(kernelContext.conv()), ioPubService)
        val o = sender.send(message)
        kotlin.test.assertTrue(o is Err)
        kotlin.test.assertTrue((o.unwrapError()) is KernelIsDownException,"should return the correct exception")
    }

    @Test
    fun send_listenerServiceIsDown() = runBlocking {
        kernelContext.startKernel()
        val mockListener = mockk<IOPubListenerService>().also {
            every { it.isRunning() } returns false
        }
        val sender = CodeExecutionSender(kernelContext.conv(), ExecuteSender(kernelContext.conv()), mockListener)
        val o = sender.send(message)
        kotlin.test.assertTrue(o is Err)
        kotlin.test.assertTrue((o.unwrapError()) is IOPubListenerNotRunningException,"should return the correct exception")
    }
}
