package com.emeraldblast.p6.message.api.message.listener

import com.github.michaelbull.result.*
import com.emeraldblast.p6.message.api.connection.kernel_context.KernelContextReadOnly
import com.emeraldblast.p6.message.api.connection.service.iopub.IOPubListenerService
import com.emeraldblast.p6.message.api.connection.service.iopub.MsgHandlerContainerImp
import com.emeraldblast.p6.message.api.connection.service.iopub.IOPubListenerServiceImp
import com.emeraldblast.p6.message.api.connection.service.iopub.handler.MsgHandlers
import com.emeraldblast.p6.message.api.message.protocol.JPRawMessage
import com.emeraldblast.p6.message.api.message.protocol.MsgType
import com.emeraldblast.p6.message.api.message.protocol.data_interface_definition.IOPub
import com.emeraldblast.p6.message.api.message.protocol.data_interface_definition.Shell
import com.emeraldblast.p6.message.api.message.sender.shell.ExecuteRequest
import com.emeraldblast.p6.test.utils.TestOnJupyter
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.zeromq.SocketType
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertFalse

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class IOPubListenerServiceImpTest : TestOnJupyter() {
    var iopSv: IOPubListenerService? = null
    @BeforeEach
    fun beforeEach(){
        this.setUp()
        runBlocking {
            kernelContext.startAll()
            kernelServiceManager.startAll()
            iopSv = kernelServiceManager.ioPubService
        }
    }

    @AfterEach
    fun afterEach(){
        runBlocking {
            kernelContext.stopAll()
        }
    }

    fun okMesg(): ExecuteRequest {
        val okMsg: ExecuteRequest = ExecuteRequest.autoCreate(
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
            UUID.randomUUID().toString()
        )
        return okMsg
    }

    val errMsg: ExecuteRequest = ExecuteRequest.autoCreate(
        sessionId = "session_id",
        username = "user_name",
        msgType = Shell.Execute.Request.msgType,
        msgContent = Shell.Execute.Request.Content(
            code = "x=1+1/*2;y=x*2;y",
            silent = false,
            storeHistory = true,
            userExpressions = mapOf(),
            allowStdin = false,
            stopOnError = true
        ),
        "msg_id_abc_123_err"
    )

    /**
     * Both Listener should be able to catch ALL pub response message from iopub channel
     */
    @Test
    fun completenessTest_MultiListeners() = runBlocking {

        val handlerWasTriggered = AtomicInteger(0)
        val handlerWasTriggered2 = AtomicInteger(0)

        // rmd: settup listener, handler
        val listener1 = IOPubListenerServiceImp(
            kernelContext = kernelContext,
            externalScope = GlobalScope,
            dispatcher = Dispatchers.IO,
        )

        listener1.addHandler(
            MsgHandlers.withUUID(
                MsgType.IOPub_execute_result,
                handlerFunction = { msg: JPRawMessage ->
                    val md = msg.toModel<IOPub.ExecuteResult.MetaData, IOPub.ExecuteResult.Content>()
                    println("Listener1 ${md.content.executionCount}")
                    handlerWasTriggered.incrementAndGet()
                },
            )
        )

        listener1.start()
        assertTrue(listener1.isRunning(), "listener 1 should be running")

        val listener2 = IOPubListenerServiceImp(
            kernelContext = kernelContext,
            externalScope = GlobalScope,
            dispatcher = Dispatchers.IO
        )

        listener2.addHandler(
            MsgHandlers.withUUID(
                MsgType.IOPub_execute_result,
                handlerFunction = { msg: JPRawMessage ->
                    val md = msg.toModel<IOPub.ExecuteResult.MetaData, IOPub.ExecuteResult.Content>()
                    println("Listener2 ${md.content.executionCount}")
                    handlerWasTriggered2.incrementAndGet()
                },
            )
        )

        listener2.start()
        assertTrue(listener2.isRunning(), "listener 2 should be running")
        // rmd: send message
        val limit = 30
        for (x in 0 until limit) {
            val okMsg: ExecuteRequest = ExecuteRequest.autoCreate(
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
                kernelContext.getMsgIdGenerator().get()?.next() ?: "zzZ"
            )
            val sender = kernelContext.getSenderProvider().unwrap().executeRequestSender()
            sender.send(okMsg)
        }
        delay(1000)
        listener1.stop()
        listener2.stop()

        assertEquals(limit, handlerWasTriggered.get(), "Listener 1 should have receive $limit messages")
        assertEquals(limit, handlerWasTriggered2.get(), "listener 2 should have receive $limit messages")
    }

    /**
     * Test sending malformed messagem
     */
    @Test
    fun testParseExceptionHandler() = runBlocking {
        // p: start a zmq pub-sub socket pair
        val pubSocket = zcontext.createSocket(SocketType.PUB).also {
            it.bind("tcp://*:5555")
        }
        val subSocket = zcontext.createSocket(SocketType.SUB).also {
            it.connect("tcp://localhost:5555")
            it.subscribe("")
        }

        // p: create a mock kernel context
        mockkStatic("com.github.michaelbull.result.UnwrapKt")
        mockkStatic("com.github.michaelbull.result.GetKt")
        val mockContext: KernelContextReadOnly = mockk<KernelContextReadOnly>().also {
            every { it.getSocketFactory().unwrap().ioPubSocket() } returns subSocket
            every { it.isKernelRunning() } returns true
            every { it.isKernelNotRunning() } returns false
//            every { kernelServiceManager.isHBAlive() } returns true
        }

        var exceptionHandlerTriggerCount = 0

        val listener = IOPubListenerServiceImp(
            mockContext,
            defaultHandler = { m -> },
            parseExceptionHandler={ e ->
                exceptionHandlerTriggerCount++
            },
            handlerContainer=MsgHandlerContainerImp(),
            externalScope = GlobalScope,
            dispatcher = Dispatchers.Default,
            startTimeOut = 50000
        )

        listener.start()

        // p: send a malformed message that cannot be parse by the listener
        pubSocket.send("malformed", 0)

        // p: wait for the msg to be handled by listener
        delay(1000)
        listener.stop()
        assertEquals(1, exceptionHandlerTriggerCount, "Exception handler should be triggered exactly once")
    }

    /**
     * default handler should be triggered if the listener does not have appropriate handler to handle certain kind of message
     */
    @Test
    fun testTriggeringDefaultHandler() = runBlocking {
        var defaultHandlerTriggeredCount = 0
        var handlerTriggeredCount = 0
        val listener = IOPubListenerServiceImp(
            kernelContext,
            defaultHandler = { msg ->
                defaultHandlerTriggeredCount++
            },
            externalScope = GlobalScope,
            dispatcher = Dispatchers.IO
        ).also {
            it.addHandler(
                MsgHandlers.withUUID(
                msgType = MsgType.Control_shutdown_reply,
                handlerFunction = { m ->
                    handlerTriggeredCount++
                },
            ))
        }

        listener.start()
        kernelContext.getSenderProvider().unwrap().executeRequestSender().also {
            it.send(okMesg())
        }
        listener.stop()
        assertEquals(1, defaultHandlerTriggeredCount, "default handler should be triggered exactly once")
        assertEquals(0, handlerTriggeredCount, "incorrect handler should not triggered")
    }

    @Test
    fun fullLifeCycle() {
        runBlocking {
            val handlerWasTriggered = AtomicInteger(0)
            val service = IOPubListenerServiceImp(
                kernelContext = kernelContext,
                externalScope = GlobalScope,
                dispatcher = Dispatchers.IO
            )
            service.addHandler(
                MsgHandlers.withUUID(MsgType.IOPub_execute_result) { msg: JPRawMessage ->
                    val md = msg.toModel<IOPub.ExecuteResult.MetaData, IOPub.ExecuteResult.Content>()
                    println(md)
                    handlerWasTriggered.incrementAndGet()
                }
            )
            val startRs = service.start()
            assertTrue(startRs is Ok, startRs.toString())
            assertTrue(service.isRunning(), "listener should be running")
            kernelContext.getSenderProvider().unwrap().executeRequestSender().also {
                it.send(okMesg())
            }
             service.stop()
            println(service.isRunning())
            assertFalse(service.isRunning(), "listener should be stopped")
            delay(300)
            assertEquals(1, handlerWasTriggered.get(), "handler should be triggered exactly once")
        }
        println("END")
    }

}
