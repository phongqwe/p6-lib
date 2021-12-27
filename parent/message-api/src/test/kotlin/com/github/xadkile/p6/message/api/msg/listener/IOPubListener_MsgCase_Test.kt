package com.github.xadkile.p6.message.api.msg.listener

import com.github.michaelbull.result.unwrap
import com.github.xadkile.p6.message.api.connection.service.iopub.IOPubListenerServiceImpl
import com.github.xadkile.p6.message.api.connection.service.iopub.MsgHandlers
import com.github.xadkile.p6.message.api.msg.protocol.JPMessage
import com.github.xadkile.p6.message.api.msg.protocol.JPRawMessage
import com.github.xadkile.p6.message.api.msg.protocol.MsgType
import com.github.xadkile.p6.message.api.msg.protocol.data_interface_definition.IOPub
import com.github.xadkile.p6.message.api.msg.protocol.data_interface_definition.Shell
import com.github.xadkile.p6.message.api.msg.protocol.data_interface_definition.handler
import com.github.xadkile.p6.message.api.msg.sender.shell.ExecuteRequest
import com.github.xadkile.p6.test.utils.TestOnJupyter
import kotlinx.coroutines.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class IOPubListener_MsgCase_Test : TestOnJupyter() {


    @AfterEach
    fun ae() {
        runBlocking {
            kernelContext.stopAll()
        }
    }

    @Test
    fun errMsg() {
        runBlocking {
            kernelContext.startAll()
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
            val listener = IOPubListenerServiceImpl(
                kernelContext = kernelContext,
                externalScope = GlobalScope,
                dispatcher = Dispatchers.Default
            )

            listener.addHandler(
                IOPub.ExecuteError.handler { msg ->
                    errHandlerWasTrigger +=1
                    val jpMsg: JPMessage<IOPub.ExecuteError.MetaData, IOPub.ExecuteError.Content> = msg.toModel()
                    println(jpMsg)
                }
            )

            listener.start()
            val o = kernelContext.getSenderProvider().unwrap().executeRequestSender().send(errMsg)
            delay(1000)
            listener.stop()
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
            kernelContext.startAll()

            var handlerWasTriggered = 0

            // rmd: settup listener, handler
            val listener = IOPubListenerServiceImpl(
                kernelContext = kernelContext,
                externalScope = GlobalScope,
                dispatcher = Dispatchers.Default
            )

            listener.addHandler(MsgHandlers.withUUID(
                    MsgType.IOPub_execute_result,
                    handlerFunction = { msg: JPRawMessage ->
                        val md = msg.toModel<IOPub.ExecuteResult.MetaData, IOPub.ExecuteResult.Content>()
                        println(md)
                        handlerWasTriggered += 1
                    },
                )
            )

            listener.start()

            // rmd: send message
            kernelContext.getSenderProvider().unwrap().executeRequestSender().also {
                it.send(okMsg, Dispatchers.Default)
            }

            listener.stop()
        }
    }
}
