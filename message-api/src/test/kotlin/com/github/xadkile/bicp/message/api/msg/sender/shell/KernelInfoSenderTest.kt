package com.github.xadkile.bicp.message.api.msg.sender.shell

import com.github.michaelbull.result.unwrap
import com.github.xadkile.bicp.message.api.msg.protocol.data_interface_definition.Shell
import com.github.xadkile.bicp.test.utils.TestOnJupyter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KernelInfoSenderTest : TestOnJupyter() {
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
            kernelContext.startKernel()
            val sender = kernelContext.getSenderProvider().unwrap().getKernelInfoSender()
            val o =sender.send(message, Dispatchers.Default)
            println(o.unwrap())
        }
    }
}
