package com.github.xadkile.bicp.message.api.sender

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.unwrap
import com.github.xadkile.bicp.message.api.msg.sender.ZMQMsgSender
import com.github.xadkile.bicp.test.utils.TestOnJupyter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import org.zeromq.ZMsg
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ZMQMsgSenderTest : TestOnJupyter() {

    @Test
    fun z(){
    }
    @Test
    fun send_Ok() {
        this.ipythonContext.startIPython()
        // send ok
        val t = System.currentTimeMillis()
        val o = ZMQMsgSender.send(
            message = listOf("a").map { it.toByteArray() },
            socket= this.zcontext.createSocket(SocketType.REQ).also {
                it.connect(this.iPythonContextReadOnly.getHeartBeatAddress().unwrap())
            },
            hbs= this.ipythonContext.getHeartBeatService().unwrap().conv(),
            interval = 1000,
            zContext = this.ipythonContext.zContext()
        )
        println(System.currentTimeMillis()-t)
        assertTrue (o is Ok)
    }
}
