package com.github.xadkile.p6.message.api.msg.listener

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.get
import com.github.michaelbull.result.unwrap
import com.github.xadkile.p6.message.api.connection.kernel_context.KernelContextReadOnlyConv
import com.github.xadkile.p6.message.api.connection.service.iopub.HandlerContainerImp
import com.github.xadkile.p6.message.api.connection.service.iopub.IOPubListenerServiceImpl
import com.github.xadkile.p6.message.api.connection.service.iopub.MsgHandlers
import com.github.xadkile.p6.message.api.msg.protocol.JPRawMessage
import com.github.xadkile.p6.message.api.msg.protocol.MsgType
import com.github.xadkile.p6.message.api.msg.protocol.data_interface_definition.IOPub
import com.github.xadkile.p6.message.api.msg.protocol.data_interface_definition.Shell
import com.github.xadkile.p6.message.api.msg.sender.shell.ExecuteRequest
import com.github.xadkile.p6.test.utils.TestOnJupyter
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.zeromq.SocketType
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class IOPubListenerServiceImplTest : TestOnJupyter() {

    fun okMesg(): ExecuteRequest {
        val okMsg: ExecuteRequest = ExecuteRequest.autoCreate(
            sessionId = "session_id",
            username = "user_name",
            msgType = Shell.Execute.Request.getMsgType(),
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
        msgType = Shell.Execute.Request.getMsgType(),
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

    @AfterEach
    fun ae() {
        runBlocking{
            kernelContext.stopAll()
        }
    }

    /**
     * Both Listener should be able to catch ALL pub response message from iopub channel
     */
    @Test
    fun completenessTest_MultiListeners() = runBlocking {

        kernelContext.startAll()

        val handlerWasTriggered = AtomicInteger(0)
        val handlerWasTriggered2 = AtomicInteger(0)

        // rmd: settup listener, handler
        val listener1 = IOPubListenerServiceImpl(
            kernelContext = kernelContext,
            externalScope = GlobalScope,
            dispatcher = Dispatchers.Default
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

        val listener2 = IOPubListenerServiceImpl(
            kernelContext = kernelContext,
            externalScope = GlobalScope,
            dispatcher = Dispatchers.Default
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

        assertTrue(listener1.isRunning(), "listener should be running")
        // rmd: send message
        val limit = 1000
        for (x in 0 until limit) {
            val okMsg: ExecuteRequest = ExecuteRequest.autoCreate(
                sessionId = "session_id",
                username = "user_name",
                msgType = Shell.Execute.Request.getMsgType(),
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
            sender.send(okMsg, Dispatchers.Default)
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
        val mockContext: KernelContextReadOnlyConv = mockk<KernelContextReadOnlyConv>().also {
            every { it.getSocketProvider().unwrap().ioPubSocket() } returns subSocket
            every { it.isKernelRunning() } returns true
            every { it.isKernelNotRunning() } returns false
            every { it.getHeartBeatService().get()?.isHBAlive() } returns true
        }

        var exceptionHandlerTriggerCount = 0

        val listener = IOPubListenerServiceImpl(
            mockContext,
            { m -> },
            { e ->
                exceptionHandlerTriggerCount++
            }, HandlerContainerImp(),
            externalScope = GlobalScope,
            dispatcher = Dispatchers.Default
        )

        listener.start()

//        Sleeper.waitUntil { listener.isRunning() }
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

        kernelContext.startAll()
        var defaultHandlerTriggeredCount = 0
        var handlerTriggeredCount = 0
        val listener = IOPubListenerServiceImpl(
            kernelContext,
            defaultHandler = { msg ->
                defaultHandlerTriggeredCount++
            },
            externalScope = GlobalScope,
            dispatcher = Dispatchers.Default
        ).also {
            it.addHandler(MsgHandlers.withUUID(
                msgType = MsgType.Control_shutdown_reply,
                handlerFunction = { m ->
                    handlerTriggeredCount++
                },
            ))
        }

        listener.start()
//        Sleeper.waitUntil { listener.isRunning() }
        // rmd: send message

        kernelContext.getSenderProvider().unwrap().executeRequestSender().also {
            it.send(okMesg(), Dispatchers.Default)
        }
        listener.stop()
        assertEquals(1, defaultHandlerTriggeredCount, "default handler should be triggered exactly once")
        assertEquals(0, handlerTriggeredCount, "incorrect handler should not triggered")
    }

    @Test
    fun fullLifeCycle() {
        runBlocking {

            val krs = kernelContext.startAll()
            assertTrue(krs is Ok,krs.toString())

            val handlerWasTriggered = AtomicInteger(0)
            // rmd: setup listener, handler
            val listener = IOPubListenerServiceImpl(
                kernelContext = kernelContext,
                externalScope = this,
                dispatcher = Dispatchers.Default
            )

            listener.addHandler(
                MsgHandlers.withUUID(MsgType.IOPub_execute_result) { msg: JPRawMessage ->
                    val md = msg.toModel<IOPub.ExecuteResult.MetaData, IOPub.ExecuteResult.Content>()
                    println(md)
                    handlerWasTriggered.incrementAndGet()
                }
            )

            val startRs = listener.start()
            assertTrue(startRs is Ok, startRs.toString())
            assertTrue(listener.isRunning(), "listener should be running")
            // rmd: send message

            kernelContext.getSenderProvider().unwrap().executeRequestSender().also {
                it.send(okMesg(), Dispatchers.Default)
            }
            listener.stop()
            assertFalse(listener.isRunning(), "listener should be stopped")
            delay(300)
            assertEquals(1, handlerWasTriggered.get(), "handler should be triggered exactly once")
        }
    }
}
