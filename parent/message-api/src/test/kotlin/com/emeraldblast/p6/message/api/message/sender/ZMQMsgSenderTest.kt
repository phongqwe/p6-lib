package com.emeraldblast.p6.message.api.message.sender

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.unwrap
import com.emeraldblast.p6.test.utils.TestOnJupyter
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

import org.zeromq.SocketType
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ZMQMsgSenderTest : TestOnJupyter() {
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
    fun send_Ok() = runBlocking{
        kernelContext.startAll()
        // send ok
        val t = System.currentTimeMillis()
        val o = ZMQMsgSender.send(
            message = listOf("a").map { it.toByteArray() },
            socket= zcontext.createSocket(SocketType.REQ).also {
                it.connect(kernelContext.getHeartBeatAddress().unwrap())
            },
            hbs= kernelServiceManager.getHeartBeatServiceRs().unwrap(),
            interval = 1000,
            zContext = kernelContext.zContext()
        )
        println(System.currentTimeMillis()-t)
        assertTrue (o is Ok,o.toString())
    }
}
