package com.github.xadkile.p6.message.api.message.sender.composite

import com.github.michaelbull.result.*
import com.github.xadkile.p6.common.exception.error.ErrorReport
import com.github.xadkile.p6.message.api.connection.kernel_context.KernelContextReadOnly
import com.github.xadkile.p6.message.api.connection.kernel_context.context_object.SenderProvider
import com.github.xadkile.p6.message.api.connection.kernel_context.errors.KernelErrors
import com.github.xadkile.p6.message.api.connection.service.iopub.HandlerContainerImp
import com.github.xadkile.p6.message.api.connection.service.iopub.IOPubListenerServiceImpl
import com.github.xadkile.p6.message.api.connection.service.iopub.errors.IOPubServiceErrors
import com.github.xadkile.p6.message.api.message.protocol.data_interface_definition.Shell
import com.github.xadkile.p6.message.api.message.sender.MsgSender
import com.github.xadkile.p6.message.api.message.sender.exception.SenderErrors
import com.github.xadkile.p6.message.api.message.sender.shell.ExecuteReply
import com.github.xadkile.p6.message.api.message.sender.shell.ExecuteRequest
import com.github.xadkile.p6.test.utils.TestOnJupyter
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CodeExecutionSenderTest : TestOnJupyter() {

    lateinit var ioPubService: IOPubListenerServiceImpl

    @AfterEach
    fun afterEach() {
        runBlocking {
            ioPubService.stop()
        }
    }

    @BeforeEach
    fun beforeEach() {
        runBlocking{
            kernelContext.startAll()
            ioPubService = IOPubListenerServiceImpl(
                kernelContext = kernelContext,
                defaultHandler = { msg ->
                    println(msg)
                },
                parseExceptionHandler = { e ->
                    println(e)
                },
                handlerContainer = HandlerContainerImp(),
                externalScope =  GlobalScope,
                dispatcher = Dispatchers.Default,
                startTimeOut = 50000
            )
            ioPubService.start()
        }
    }

    val message: ExecuteRequest = ExecuteRequest.autoCreate(
        sessionId = "session_id",
        username = "user_name",
        msgType = Shell.Execute.Request.msgType,
        msgContent = Shell.Execute.Request.Content(
            code ="""
                x=0
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
//    @Test
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
                        kernelContext = kernelContext,
                    )

                    val o = sender.send(message)
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
            val sender = CodeExecutionSender(kernelContext)
            val o = sender.send(message)
            assertTrue(o is Ok, o.toString())
        }
    }

    /**
     * Ensure that long operations is wait until they are completed
     */
//    @Test
    fun send_Ok_longOperation() {
        runBlocking {
            val message2: ExecuteRequest = ExecuteRequest.autoCreate(
                sessionId = "session_id",
                username = "user_name",
                msgType = Shell.Execute.Request.msgType,
                msgContent = Shell.Execute.Request.Content(
                    code =
                            "x=0\n" + "" +
                            "while(True):\n"+
                            "    x=x+1\n"+
                            "    if(x>200000000):\n"+
                            "        break\n"
                    ,
                    silent = false,
                    storeHistory = true,
                    userExpressions = mapOf(),
                    allowStdin = false,
                    stopOnError = true
                ),
                "msg_id_abc_2"
            )
            val message: ExecuteRequest = ExecuteRequest.autoCreate(
                sessionId = "session_id",
                username = "user_name",
                msgType = Shell.Execute.Request.msgType,
                msgContent = Shell.Execute.Request.Content(
                    code = "x",
                    silent = false,
                    storeHistory = true,
                    userExpressions = mapOf(),
                    allowStdin = false,
                    stopOnError = true
                ),
                "msg_id_abc_1"
            )
            val sender = CodeExecutionSender(kernelContext)
            val o2 = sender.send(message2)
            assertTrue(o2 is Ok,o2.toString())
            assertNull(o2.value)

            val o = sender.send(message)
            assertTrue(o is Ok, o.toString())
            assertNotNull(o.value)
            println(o.value?.content)
        }
    }

    /**
     * When unable to send a message, the composite sender should return an Err indicate such condition
     */
    @Test
    fun send_fail() {
        runBlocking {
            kernelContext.startAll()

            // ph: mockk is horribly slow here
            val mockSender = object : MsgSender<ExecuteRequest, Result<ExecuteReply, ErrorReport>> {
                override suspend fun send(
                    message: ExecuteRequest,
                ): Result<ExecuteReply, ErrorReport> {
                    return Err(
                        ErrorReport(
                        header = SenderErrors.UnableToSendMsg,
                        data = SenderErrors.UnableToSendMsg.Data(message)
                    )
                    )
                }
            }

            val mockSenderProvider = mockk<SenderProvider>().also {
                every{it.executeRequestSender()} returns mockSender
            }
            val mockContext = spyk(kernelContext).also {
                every{it.getSenderProvider()} returns Ok(mockSenderProvider)
            }

            val sender = CodeExecutionSender(mockContext)
            val o = sender.send(message)
            assertTrue(o is Err, o.toString())
            assertTrue(o.unwrapError().header is SenderErrors.UnableToSendMsg)
        }
    }

    @Test
    fun send_kernelNotRunning() = runBlocking {
        kernelContext.stopAll()
        val sender = CodeExecutionSender(kernelContext)
        val o = sender.send(message)
        assertTrue(o is Err)
        assertTrue((o.unwrapError().header) is KernelErrors.KernelDown,"should return the correct exception")
    }

    @Test
    fun send_listenerServiceIsDown() = runBlocking {
        kernelContext.startAll()
        val mockListener = mockk<IOPubListenerServiceImpl>().also {
            every { it.isRunning() } returns false
            every { it.isNotRunning() } returns true
        }

        val mockContext: KernelContextReadOnly = spyk(kernelContext).also {
            every {it.getIOPubListenerService()} returns Ok(mockListener)
        }

        val sender = CodeExecutionSender(mockContext)
        val o = sender.send(message)
        assertTrue(o is Err)
        assertTrue((o.unwrapError().header) is IOPubServiceErrors.IOPubServiceNotRunning,"should return the correct exception")
    }

    @Test
    fun test_sendMalformedCode() {
        runBlocking{
            kernelContext.startAll()
            val malformedCodeMsg: ExecuteRequest = ExecuteRequest.autoCreate(
                sessionId = "session_id",
                username = "user_name",
                msgType = Shell.Execute.Request.msgType,
                msgContent = Shell.Execute.Request.Content(
                    code = "x=1+2*2;functionX()",
                    silent = false,
                    storeHistory = true,
                    userExpressions = mapOf(),
                    allowStdin = false,
                    stopOnError = true
                ),
                "msg_id_abc_123"
            )
            val sender = CodeExecutionSender(kernelContext)
            val o = sender.send(malformedCodeMsg)
            assertTrue(o is Err)
        }
    }
}
