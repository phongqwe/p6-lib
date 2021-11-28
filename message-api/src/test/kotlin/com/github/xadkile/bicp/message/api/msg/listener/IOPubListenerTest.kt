package com.github.xadkile.bicp.message.api.msg.listener

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.unwrap
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelContextReadOnlyConv
import com.github.xadkile.bicp.message.api.connection.kernel_context.SocketProvider
import com.github.xadkile.bicp.message.api.msg.protocol.message.JPRawMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgType
import com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition.IOPub
import com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition.Shell
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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class IOPubListenerTest : TestOnJupyter() {
    val msg: ExecuteRequest = ExecuteRequest.autoCreate(
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

    @AfterEach
    fun ae() {
        kernelContext.stopKernel()
    }

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
        val mockContext = mockk<KernelContextReadOnlyConv>().also {
            every { it.getSocketProvider().unwrap().ioPubSocket() } returns subSocket
            every {it.isRunning()} returns true
            every {it.isNotRunning()} returns false
        }

        var exceptionHandlerTriggerCount = 0

        val listener = IOPubListener(mockContext,
            { m, l -> },
            { e, l -> exceptionHandlerTriggerCount++
            l.stop()})

        listener.start(this, Dispatchers.Default)

        Sleeper.waitUntil { listener.isRunning() }
        // p: send a malformed message that cannot be parse by the listener
        pubSocket.send("malformed", 0)

        // p: wait for the msg to be handled by listener
        delay(1000)
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
            defaultHandler = { msg, l ->
                defaultHandlerTriggeredCount++
                l.stop()
            }
        ).also {
            it.addHandler(MsgHandlers.withUUID(MsgType.Control_shutdown_reply) { m, l ->
                handlerTriggeredCount++
            })
        }

        listener.start(this, Dispatchers.Default)
        Sleeper.waitUntil { listener.isRunning() }
        // rmd: send message

        kernelContext.getSenderProvider().unwrap().getExecuteRequestSender().also {
            it.send(msg, Dispatchers.Default)
        }
        delay(1000)
        assertEquals(1, defaultHandlerTriggeredCount, "default handler should be triggered exactly once")
        assertEquals(0, handlerTriggeredCount, "incorrect handler should not triggered")
    }

    @Test
    fun fullLifeCycle() {
        runBlocking {

            kernelContext.startKernel()

            var handlerWasTriggered = 0

            // rmd: settup listener, handler
            val listener = IOPubListener(
                kernelContext = kernelContext,
            )

            listener.addHandler(MsgHandlers.withUUID(MsgType.IOPub_execute_result) { msg: JPRawMessage, l: MsgListener ->
                val md = msg.toModel<IOPub.ExecuteResult.MetaData, IOPub.ExecuteResult.Content>()
                println(md)
                handlerWasTriggered += 1
                listener.stop()
            })

            listener.start(this, Dispatchers.Default)

            Sleeper.waitUntil { listener.isRunning() }
            assertTrue(listener.isRunning(), "listener should be running")
            // rmd: send message

            kernelContext.getSenderProvider().unwrap().getExecuteRequestSender().also {
                it.send(msg, Dispatchers.Default)
            }
            delay(1000)
            assertFalse(listener.isRunning(), "listener should be stopped")
            assertEquals(1, handlerWasTriggered, "handler should be triggered exactly once")
        }
    }
}
