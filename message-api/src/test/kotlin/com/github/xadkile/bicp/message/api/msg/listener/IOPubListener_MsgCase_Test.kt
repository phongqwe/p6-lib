package com.github.xadkile.bicp.message.api.msg.listener

import com.github.michaelbull.result.unwrap
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelContextReadOnlyConv
import com.github.xadkile.bicp.message.api.msg.protocol.message.JPMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.JPRawMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgType
import com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition.IOPub
import com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition.Shell
import com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition.handler
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
internal class IOPubListener_MsgCase_Test : TestOnJupyter() {


    @AfterEach
    fun ae() {
        kernelContext.stopKernel()
    }

    @Test
    fun errMsg() {
        runBlocking {
            kernelContext.startKernel()
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
            var errHandlerWasTrigger = 0
            val listener = IOPubListener(
                kernelContext = kernelContext,
            )

            listener.addHandler(
                IOPub.Error.handler { msg, listener ->
                    errHandlerWasTrigger +=1
                    println(msg)
                    val jpMsg:JPMessage<IOPub.Error.MetaData,IOPub.Error.Content> = msg.toModel()
                    println(jpMsg)
                }
            )

            listener.start(this, Dispatchers.Default)
            val sr = kernelContext.getSenderProvider().unwrap().getExecuteRequestSender().send(errMsg)
            listener.stop()
            Thread.sleep(1000)
            assertEquals(1,errHandlerWasTrigger, "error handler should be triggered exactly once")

        }
    }


    @Test
    fun okMsg() {
        runBlocking {
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
                "msg_id_abc_123"
            )
            kernelContext.startKernel()

            var handlerWasTriggered = 0

            // rmd: settup listener, handler
            val listener = IOPubListener(
                kernelContext = kernelContext,
            )

            listener.addHandler(MsgHandlers.withUUID(
                    MsgType.IOPub_execute_result,
                    handlerFunction = { msg: JPRawMessage, l: MsgListener ->
                        val md = msg.toModel<IOPub.ExecuteResult.MetaData, IOPub.ExecuteResult.Content>()
                        println(md)
                        handlerWasTriggered += 1
                        listener.stop()
                    },
                )
            )

            listener.start(this, Dispatchers.Default)

            // rmd: send message
            kernelContext.getSenderProvider().unwrap().getExecuteRequestSender().also {
                it.send(okMsg, Dispatchers.Default)
            }
            delay(1000)
        }
    }
}
