package com.github.xadkile.bicp.message.api.msg.listener

import com.github.michaelbull.result.unwrap
import com.github.xadkile.bicp.message.api.msg.protocol.message.JPRawMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgType
import com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition.IOPub
import com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition.Shell
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteRequest
import com.github.xadkile.bicp.message.api.other.Sleeper
import com.github.xadkile.bicp.test.utils.TestOnJupyter
import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class IOPubListenerTest : TestOnJupyter() {
    @Test
    fun start() {
        runBlocking {

            kernelContext.startKernel()

            var handlerWasTriggered = 0
            // rmd: settup listener, handler
            val listener = IOPubListener(
                kernelContext = kernelContext.conv(),
                defaultHandler = {
                    println(it.identities)
                },
            )

            listener.addHandler(MsgHandlers.withUUID(MsgType.IOPub_execute_result) { msg: JPRawMessage ->
                val md = msg.toModel<IOPub.ExecuteResult.MetaData, IOPub.ExecuteResult.Content>()
                println(md)
                handlerWasTriggered += 1
                listener.stop()
            })
            coroutineScope {
                launch {
                    listener.start(this,mainThreadSurrogate)
                }
                launch {
                    Sleeper.waitUntil { listener.isRunning() }
                    assertTrue(listener.isRunning(), "listener should be running")
                    // rmd: send message
                    val msg:ExecuteRequest = ExecuteRequest.autoCreate(
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
                    println(msg.header)
                    kernelContext.getSenderProvider().unwrap().getExecuteRequestSender().also {
                        it.send(msg,Dispatchers.Default)
                    }

                    delay(1000)

                    assertFalse(listener.isRunning(), "listener should be stopped")
                    assertEquals(1, handlerWasTriggered, "handler should be triggered exactly once")
                }
            }
        }
    }

    @Test
    fun stop() {
    }
}
