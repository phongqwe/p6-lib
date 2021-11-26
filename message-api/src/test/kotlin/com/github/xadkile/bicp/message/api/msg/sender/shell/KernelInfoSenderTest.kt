package com.github.xadkile.bicp.message.api.msg.sender.shell

import com.github.michaelbull.result.unwrap
import com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition.Shell
import com.github.xadkile.bicp.test.utils.TestOnJupyter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KernelInfoSenderTest : TestOnJupyter() {
    @Test
    fun send_Ok() {

        val message:KernelInfoInput = KernelInfoInput.autoCreate(
            sessionId = "session_id",
            username = "user_name",
            msgType = Shell.KernelInfo.Request.msgType,
            msgContent = Shell.KernelInfo.Request.Content(),
            "msg_id_abc_123"
        )
        this.kernelContext.startKernel()
        val sender = this.kernelContext.getSenderProvider().unwrap().getKernelInfoSender()
        val o =sender.send(message)
        println(o.unwrap())
    }
}
