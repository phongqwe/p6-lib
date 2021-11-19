package com.github.xadkile.bicp.message.api.sender

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.unwrap
import com.github.xadkile.bicp.test.utils.TestOnJupyter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

import org.zeromq.SocketType
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ZMQMsgSenderTest : TestOnJupyter() {

    @Test
    fun send_Ok() {
        this.ipythonContext.startIPython()
        // send ok
        val t = System.currentTimeMillis()
        val o = ZMQMsgSender.send(
            message = listOf("a").map { it.toByteArray() },
            socket= this.zcontext.createSocket(SocketType.REQ).also {
                it.connect(this.ipythonContext.getHeartBeatAddress().unwrap())
            },
            ipContext = this.ipythonContext,
            interval = 1000,
        )
        println(System.currentTimeMillis()-t)
        assertTrue (o is Ok)
    }

//    @Test
//    fun send_Fail() {
//        this.ipythonContext.stopIPython()
//        val o = ZMQMsgSender.send(
//            socket= this.zcontext.createSocket(SocketType.REQ).also {
//                it.connect(this.ipythonContext.getHeartBeatAddress().unwrap())
//            },
//            ipContext = this.ipythonContext,
//            interval = 1000,
//            "a"
//        )
//        // send fail
//        assertNull(o)
//    }
}
