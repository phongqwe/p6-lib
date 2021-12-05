package com.github.xadkile.bicp.message.api.msg.listener

import com.github.michaelbull.result.get
import com.github.michaelbull.result.unwrap
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelContextReadOnlyConv
import com.github.xadkile.bicp.message.api.msg.protocol.JPRawMessage
import com.github.xadkile.bicp.message.api.msg.protocol.MsgType
import com.github.xadkile.bicp.message.api.msg.protocol.data_interface_definition.IOPub
import com.github.xadkile.bicp.message.api.msg.protocol.data_interface_definition.Shell
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteRequest
import com.github.xadkile.bicp.message.api.other.Sleeper
import com.github.xadkile.bicp.test.utils.TestOnJupyter
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.zeromq.SocketType
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class IOPubListenerTest : TestOnJupyter() {

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

    @AfterEach
    fun ae() {
        kernelContext.stopKernel()
    }

    /**
     * Both Listener should be able to catch ALL pub response message from iopub channel
     */
    @Test
    fun completenessTest_MultiListeners() = runBlocking {

        kernelContext.startKernel()

        val handlerWasTriggered = AtomicInteger(0)
        val handlerWasTriggered2 = AtomicInteger(0)

        // rmd: settup listener, handler
        val listener1 = IOPubListener(
            kernelContext = kernelContext
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

        listener1.start(this, Dispatchers.Default)

        val listener2 = IOPubListener(
            kernelContext = kernelContext,
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

        listener2.start(this, Dispatchers.Default)

        Sleeper.waitUntil { listener1.isRunning() }
        Sleeper.waitUntil { listener2.isRunning() }

        assertTrue(listener1.isRunning(), "listener should be running")
        // rmd: send message
        val limit = 1000
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
            sender.send(okMsg, Dispatchers.Default)
        }
        listener1.stop()
        listener2.stop()

        assertEquals(limit, handlerWasTriggered.get(), "Listener 1 should have receive $limit messages")
        assertEquals(limit, handlerWasTriggered2.get(), "listener 2 should have receive $limit messages")
    }

    /**
     * Test sending malformed message
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
            every { it.isRunning() } returns true
            every { it.isNotRunning() } returns false
            every { it.getConvHeartBeatService().get()?.isHBAlive() } returns true
        }

        var exceptionHandlerTriggerCount = 0

        val listener = IOPubListener(
            mockContext,
            { m -> },
            { e ->
                exceptionHandlerTriggerCount++
            }, HandlerContainerImp())

        listener.start(this, Dispatchers.Default)

        Sleeper.waitUntil { listener.isRunning() }
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

        kernelContext.startKernel()
        var defaultHandlerTriggeredCount = 0
        var handlerTriggeredCount = 0
        val listener = IOPubListener(
            kernelContext,
            defaultHandler = { msg ->
                defaultHandlerTriggeredCount++
            }
        ).also {
            it.addHandler(MsgHandlers.withUUID(
                msgType = MsgType.Control_shutdown_reply,
                handlerFunction = { m ->
                    handlerTriggeredCount++
                },
            ))
        }

        listener.start(this, Dispatchers.Default)
        Sleeper.waitUntil { listener.isRunning() }
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

            kernelContext.startKernel()

            for (x in 0 until 200) {
                val handlerWasTriggered = AtomicInteger(0)
                // rmd: setup listener, handler
                val listener = IOPubListener(
                    kernelContext = kernelContext
                )

                listener.addHandler(
                    MsgHandlers.withUUID(MsgType.IOPub_execute_result) { msg: JPRawMessage ->
                        launch(Dispatchers.Default){
                            val md = msg.toModel<IOPub.ExecuteResult.MetaData, IOPub.ExecuteResult.Content>()
                            println(md)
                            handlerWasTriggered.incrementAndGet()
                        }
                    }
                )

                listener.start(GlobalScope, Dispatchers.Default)

                assertTrue(listener.isRunning(), "listener should be running")
                // rmd: send message

                kernelContext.getSenderProvider().unwrap().executeRequestSender().also {
                    it.send(okMesg(), Dispatchers.Default)
                }
                listener.stop()
                assertFalse(listener.isRunning(), "listener should be stopped")
//                assertEquals(1, handlerWasTriggered.get(), "handler should be triggered exactly once")
            }
        }
    }
}
