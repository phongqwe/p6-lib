package com.emeraldblast.p6.message.api.message.sender.shell

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.unwrap
import com.emeraldblast.p6.message.api.message.protocol.data_interface_definition.Shell
import com.emeraldblast.p6.test.utils.TestOnJupyter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KernelInfoSenderTest : TestOnJupyter() {
    @BeforeEach
    fun beforeEach(){
        this.setUp()
        runBlocking {
            kernelContext.startAll()
        }
    }

    @AfterEach
    fun afterEach(){
        runBlocking {
            kernelContext.stopAll()
        }
    }
    @Test
    fun send_Ok() {
        runBlocking {
            val message:KernelInfoInput = KernelInfoInput.autoCreate(
                sessionId = "session_id",
                username = "user_name",
                msgType = Shell.KernelInfo.Request.msgType,
                msgContent = Shell.KernelInfo.Request.Content(),
                "msg_id_abc_123"
            )
            val sender = kernelContext.getSenderProvider().unwrap().kernelInfoSender()
            val o =sender.send(message)
            assertTrue(o is Ok)
            println(o.unwrap())
        }
    }
}
