package com.github.xadkile.bicp.message.api.connection.heart_beat

import com.github.michaelbull.result.unwrap
import com.github.xadkile.bicp.message.api.connection.ipython_context.SocketProvider
import com.github.xadkile.bicp.test.utils.TestOnJupyter
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.zeromq.SocketType
import org.zeromq.ZMQ

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class LiveCountHeartBeatServiceUpdatableTest : TestOnJupyter() {
    lateinit var hbService: LiveCountHeartBeatServiceUpdatable
    val liveCount = 4
    val interval:Long = 500

    @BeforeEach
    fun beforeEach() {
        this.ipythonContext.startIPython()
        hbService = LiveCountHeartBeatServiceUpdatable(
            this.zcontext,newSocketProvider(), liveCount, interval,
        )
        this.ipythonContext.setOnStartProcessListener{context->
            hbService.updateSocket(newSocketProvider())
        }
    }

    @AfterEach
    fun afterEach(){
        hbService.stop()
        this.ipythonContext.stopIPython()
    }

    @Test
    fun start() {
        this.ipythonContext.startIPython()
        hbService.start()
        assertTrue(hbService.isServiceRunning())
        Thread.sleep(1000)
        assertTrue(hbService.isHBAlive())

        this.ipythonContext.stopIPython()
        assertTrue(hbService.isServiceRunning())
        Thread.sleep(1000)
        assertFalse(hbService.isHBAlive())

        this.ipythonContext.startIPython()
        Thread.sleep(1000)
        assertTrue(hbService.isServiceRunning())
        assertTrue(hbService.isHBAlive())
    }

    private fun newSocket():ZMQ.Socket{
        return this.zcontext.createSocket(SocketType.REQ).also {
            it.connect(this.ipythonContext.getChannelProvider().unwrap().heartbeatChannel().makeAddress())
        }
    }
    private fun newSocketProvider():SocketProvider{
        return this.ipythonContext.getSocketProvider().unwrap()
    }
}
