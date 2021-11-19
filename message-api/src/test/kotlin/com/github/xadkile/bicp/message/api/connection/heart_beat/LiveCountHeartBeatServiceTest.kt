package com.github.xadkile.bicp.message.api.connection.heart_beat

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.unwrap
import com.github.michaelbull.result.unwrapError
import com.github.xadkile.bicp.test.utils.TestOnJupyter
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.zeromq.SocketType
import org.zeromq.ZMQ
import org.zeromq.ZMQException

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class LiveCountHeartBeatServiceTest : TestOnJupyter() {
    lateinit var hbService: LiveCountHeartBeatService
    lateinit var socket: ZMQ.Socket
    val liveCount = 4
    val interval:Long = 500

    @BeforeEach
    fun beforeEach() {
        this.ipythonContext.startIPython()
        socket = this.zcontext.createSocket(SocketType.REQ).also {
            it.connect(this.ipythonContext.getChannelProvider().unwrap().getHeartbeatChannel().makeAddress())
        }
        hbService = LiveCountHeartBeatService(
            this.zcontext,socket, liveCount, interval, 1000,
        )
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
        assertNotNull(hbService.getThread())
        assertTrue(hbService.getThread()?.isAlive ?: false)
    }

    @Test
    fun isAlive() {
        this.ipythonContext.startIPython()
        hbService.start()
        assertTrue(hbService.isServiceRunning())
        Thread.sleep(1000)
        assertTrue(hbService.isHBAlive())
    }

    @Test
    fun stop() {
        this.ipythonContext.startIPython()
        hbService.start()
        hbService.stop()
        assertFalse(hbService.isServiceRunning())
    }

    @Test
    fun checkOk(){
        this.ipythonContext.startIPython()
        hbService.start()
        assertTrue(hbService.checkHB() is Ok)
    }

    @Test
    fun checkFalse(){
        hbService.start()
        this.ipythonContext.stopIPython()
        val cr= hbService.checkHB()
        assertTrue(cr is Err)
        assertTrue(cr.unwrapError() is ZMQException)

    }

    @Test
    fun checkWithStoppedService(){
        hbService.stop()
        this.ipythonContext.stopIPython()
        val cr = hbService.checkHB()
        assertTrue(cr is Err)
        assertTrue(cr.unwrapError() is HeartBeatService.NotRunningException)
    }
}
