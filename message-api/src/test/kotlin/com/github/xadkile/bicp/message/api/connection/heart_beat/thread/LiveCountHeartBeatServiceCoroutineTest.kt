package com.github.xadkile.bicp.message.api.connection.heart_beat.thread

import com.github.michaelbull.result.unwrap
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatService
import com.github.xadkile.bicp.test.utils.TestOnJupyter
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.zeromq.SocketType
import org.zeromq.ZMQ

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class LiveCountHeartBeatServiceThreadTest : TestOnJupyter() {
    lateinit var hbService: HeartBeatService
    lateinit var hbService2: HeartBeatService
    lateinit var socket: ZMQ.Socket
    val liveCount = 4
    val interval:Long = 500

    @BeforeEach
    fun beforeEach() {
        this.kernelContext.startKernel()
        socket = this.zcontext.createSocket(SocketType.REQ).also {
            it.connect(this.kernelContext.getChannelProvider().unwrap().heartbeatChannel().makeAddress())
        }
        hbService2 = LiveCountHeartBeatServiceThread(
            this.zcontext,this.kernelContext.getSocketProvider().unwrap(), liveCount, interval,
        )
        hbService = this.kernelContext.getHeartBeatService().unwrap()
    }

    @AfterEach
    fun afterEach(){
        hbService.stop()
        this.kernelContext.stopKernel()
    }

    @Test
    fun dumm(){
        this.kernelContext.startKernel()
        hbService2.start()
    }

    @Test
    fun start() {
        this.kernelContext.startKernel()
        assertTrue(hbService.isServiceRunning())
    }

    @Test
    fun isAlive() {
        this.kernelContext.startKernel()
        hbService.start()
        assertTrue(hbService.isServiceRunning())
        Thread.sleep(1000)
        assertTrue(hbService.isHBAlive())
    }

    @Test
    fun stop() {
        this.kernelContext.startKernel()
        hbService.start()
        hbService.stop()
        assertFalse(hbService.isServiceRunning())
    }
}
