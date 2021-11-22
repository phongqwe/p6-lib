package com.github.xadkile.bicp.message.api.msg.sender

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.unwrap
import com.github.xadkile.bicp.message.api.other.Sleeper
import com.github.xadkile.bicp.test.utils.TestOnJupyter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

import org.zeromq.SocketType
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ZMQMsgSenderTest : TestOnJupyter() {

    @Test
    fun send_Ok() {
        this.ipythonContext.startIPython()
//        Sleeper.sleepUntil(50){this.ipythonContext.getHeartBeatService().unwrap().isHBAlive()}
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
        assertTrue (o is Ok,o.toString())
    }
}
