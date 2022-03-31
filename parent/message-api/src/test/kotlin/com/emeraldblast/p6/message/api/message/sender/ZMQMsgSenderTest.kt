package com.emeraldblast.p6.message.api.message.sender

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.unwrap
import com.emeraldblast.p6.test.utils.TestOnJupyter
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

import org.zeromq.SocketType
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ZMQMsgSenderTest : TestOnJupyter() {

    @Test
    fun send_Ok() = runBlocking{
        kernelContext.startAll()
        // send ok
        val t = System.currentTimeMillis()
        val o = ZMQMsgSender.send(
            message = listOf("a").map { it.toByteArray() },
            socket= zcontext.createSocket(SocketType.REQ).also {
                it.connect(iPythonContextConv.getHeartBeatAddress().unwrap())
            },
            hbs= kernelContext.getHeartBeatService().unwrap(),
            interval = 1000,
            zContext = kernelContext.zContext()
        )
        println(System.currentTimeMillis()-t)
        assertTrue (o is Ok,o.toString())
    }
}
