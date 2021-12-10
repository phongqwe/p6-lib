package com.github.xadkile.bicp.message.api.msg.sender.composite

import com.github.michaelbull.result.*
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelContextReadOnlyConv
import com.github.xadkile.bicp.message.api.connection.kernel_context.context_object.SenderProvider
import com.github.xadkile.bicp.message.api.connection.kernel_context.exception.KernelIsDownException
import com.github.xadkile.bicp.message.api.connection.service.iopub.HandlerContainerImp
import com.github.xadkile.bicp.message.api.connection.service.iopub.IOPubListenerServiceImpl
import com.github.xadkile.bicp.message.api.connection.service.iopub.exception.IOPubListenerNotRunningException
import com.github.xadkile.bicp.message.api.msg.protocol.data_interface_definition.Shell
import com.github.xadkile.bicp.message.api.msg.sender.MsgSender
import com.github.xadkile.bicp.message.api.msg.sender.exception.UnableToSendMsgException
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteReply
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteRequest
import com.github.xadkile.bicp.test.utils.TestOnJupyter
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CodeExecutionSenderTest : TestOnJupyter() {

    lateinit var ioPubService: IOPubListenerServiceImpl

    @AfterEach
    fun ae() {
        runBlocking {
            ioPubService.stop()
            kernelContext.stopAll()
        }
    }

    @BeforeEach
    fun beforeEach() {
        runBlocking{
            kernelContext.startAll()
            ioPubService = IOPubListenerServiceImpl(
                kernelContext = kernelContext.conv(),
                defaultHandler = { msg ->
                    println(msg)
                },
                parseExceptionHandler = { e ->
                    println(e)
                },
                handlerContainer = HandlerContainerImp(),
                externalScope =  GlobalScope,
                dispatcher = Dispatchers.Default
            )
            ioPubService.start()
//        println(ioPubService.isRunning())
        }
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
            val sender = CodeExecutionSender(kernelContext.conv())
            val o = sender.send(message, Dispatchers.Default)
            assertTrue(o is Ok, o.toString())
            println(o.value.content)
        }
    }

//    @Test
    fun send_Ok2() {
        runBlocking {
            val message2: ExecuteRequest = ExecuteRequest.autoCreate(
                sessionId = "session_id",
                username = "user_name",
                msgType = Shell.Execute.Request.msgType,
                msgContent = Shell.Execute.Request.Content(
                    code ="""
                x=0
                while(True):
                    x+=1
            """.trimIndent(),
                    silent = false,
                    storeHistory = true,
                    userExpressions = mapOf(),
                    allowStdin = false,
                    stopOnError = true
                ),
                "msg_id_abc_123"
            )
            val sender = CodeExecutionSender(kernelContext.conv())
            val o2 = sender.send(message2, Dispatchers.Default)
            val o = sender.send(message,Dispatchers.Default)
            assertTrue(o is Ok, o.toString())
            println(o.value.content)
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
            val mockSender = object : MsgSender<ExecuteRequest, Result<ExecuteReply, Exception>> {
                override suspend fun send(
                    message: ExecuteRequest,
                    dispatcher: CoroutineDispatcher,
                ): Result<ExecuteReply, Exception> {
                    return Err(UnableToSendMsgException(message))
                }
            }

            val mockSenderProvider = mockk<SenderProvider>().also {
                every{it.executeRequestSender()} returns mockSender
            }
            val mockContext = spyk(kernelContext.conv()).also {
                every{it.getSenderProvider()} returns Ok(mockSenderProvider)
            }

            val sender = CodeExecutionSender(mockContext)
            val o = sender.send(message, Dispatchers.Default)
            assertTrue(o is Err, o.toString())
            assertTrue(o.unwrapError() is UnableToSendMsgException)
            assertEquals(message, (o.unwrapError() as UnableToSendMsgException).getMsg())
        }
    }

    @Test
    fun send_kernelNotRunning() = runBlocking {
        kernelContext.stopAll()
        val sender = CodeExecutionSender(kernelContext.conv())
        val o = sender.send(message)
        assertTrue(o is Err)
        assertTrue((o.unwrapError()) is KernelIsDownException,"should return the correct exception")
    }

    @Test
    fun send_listenerServiceIsDown() = runBlocking {
        kernelContext.startAll()
        val mockListener = mockk<IOPubListenerServiceImpl>().also {
            every { it.isRunning() } returns false
            every { it.isNotRunning() } returns true
        }

        val mockContext:KernelContextReadOnlyConv = spyk(kernelContext.conv()).also {
            every {it.getIOPubListenerService()} returns Ok(mockListener)
        }

        val sender = CodeExecutionSender(mockContext)
        val o = sender.send(message)
        kotlin.test.assertTrue(o is Err)
        kotlin.test.assertTrue((o.unwrapError()) is IOPubListenerNotRunningException,"should return the correct exception")
    }
}
