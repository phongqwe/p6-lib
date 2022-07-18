package com.emeraldblast.p6.message.api.message.listener

import com.github.michaelbull.result.unwrap
import com.emeraldblast.p6.message.api.connection.service.iopub.IOPubListenerServiceImp
import com.emeraldblast.p6.message.api.connection.service.iopub.handler.MsgHandlers
import com.emeraldblast.p6.message.api.message.protocol.JPMessage
import com.emeraldblast.p6.message.api.message.protocol.JPRawMessage
import com.emeraldblast.p6.message.api.message.protocol.MsgType
import com.emeraldblast.p6.message.api.message.protocol.data_interface_definition.IOPub
import com.emeraldblast.p6.message.api.message.protocol.data_interface_definition.Shell
import com.emeraldblast.p6.message.api.message.sender.shell.ExecuteRequest
import com.emeraldblast.p6.test.utils.TestOnJupyter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.concurrent.atomic.AtomicInteger


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class IOPubListener_MsgCase_Test : TestOnJupyter() {
    @BeforeEach
    fun beforeEach(){
        this.setUp()
        runBlocking {
            kernelContext.startAll()
            kernelServiceManager.startAll()
        }
    }

    @AfterEach
    fun afterEach(){
        runBlocking {
            kernelContext.stopAll()
        }
    }

    @Test
    fun errMsg() {
        runBlocking {
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
            var errHandlerWasTrigger = AtomicInteger(0)
            val listener = IOPubListenerServiceImp(
                kernelContext = kernelContext,
                externalScope = GlobalScope,
                dispatcher = Dispatchers.Default
            )

            listener.addHandler(
                IOPub.ExecuteError.handler { msg ->
                    errHandlerWasTrigger.incrementAndGet()
                    val jpMsg: JPMessage<IOPub.ExecuteError.MetaData, IOPub.ExecuteError.Content> = msg.toModel()
                    println(jpMsg)
                }
            )

            listener.start()
            val o = kernelContext.getSenderProvider().unwrap().executeRequestSender().send(errMsg)
            delay(1000)
            listener.stop()
            assertEquals(1,errHandlerWasTrigger.get(), "error handler should be triggered exactly once")
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

            var handlerWasTriggered = 0

            // rmd: settup listener, handler
            val listener = IOPubListenerServiceImp(
                kernelContext = kernelContext,
                externalScope = GlobalScope,
                dispatcher = Dispatchers.Default
            )

            listener.addHandler(
                MsgHandlers.withUUID(
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
                it.send(okMsg)
            }

            listener.stop()
        }
    }
}
