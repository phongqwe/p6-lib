package com.github.xadkile.bicp.message.api.connection.heart_beat

import com.github.michaelbull.result.Ok
import com.github.xadkile.bicp.message.api.connection.ipython_context.SocketProvider
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.util.*
import kotlin.concurrent.thread


/**
 * This service can exist independently from [IPythonContext]
 * TODO what is the point of this class, if the kernel is dead, there's no point in running this services.
 */
internal class LiveCountHeartBeatServiceUpdatable constructor(
    zContext: ZContext,
    private var socketProvider: SocketProvider,
    liveCount: Int = 3,
    pollTimeout: Long = 1000,
) : HeartBeatServiceUpdatable,
    AbstractLiveCountHeartBeatService(zContext, liveCount,pollTimeout) {

    private val convService = HeartBeatServiceConvImp(this)
    private val updateEventList: Queue<UpdateEvent> = ArrayDeque()

    override fun updateSocket(socketProvider: SocketProvider) {
        updateEventList.offer {
            this.socketProvider = socketProvider
            UpdateSignal.UPDATE_SOCKET
        }
    }

    /**
     * init resources and start service thread
     */
    override fun start(): Boolean {
        this.letThreadRunning = true
        this.serviceThread = thread(true) {
            val thisObj = this@LiveCountHeartBeatServiceUpdatable
            var poller = zContext.createPoller(1)
            var socket = this.socketProvider.heartBeatSocket()
            poller.register(socket,ZMQ.Poller.POLLIN)
            poller.use {
                while (letThreadRunning) {
                    // rmd: consume update events before doing anything
                    while (this.updateEventList.isNotEmpty()) {
                        val event:UpdateEvent = this.updateEventList.poll()
                        val updateSignal:UpdateSignal = event.consum()
                        when (updateSignal) {
                            UpdateSignal.UPDATE_SOCKET -> {
                                poller.close()
                                socket.close()
                                poller = zContext.createPoller(1)
                                socket = this.socketProvider.heartBeatSocket()
                                poller.register(socket,ZMQ.Poller.POLLIN)
                            }
                            else -> {
                            }
                        }
                    }

                    val isAlive: Boolean = thisObj.check(poller, socket) is Ok
                    if (isAlive) {
                        this.currentLives = this.liveCount
                    } else {
                        // rmd: only reduce life if there are lives left to prevent underflow of int
                        if (this.currentLives > 0) {
                            this.currentLives -= 1
                        }
                    }
                }
            }
        }
        return true
    }

    internal fun getThread(): Thread? {
        return this.serviceThread
    }


    override fun conv(): HeartBeatServiceConv {
        return this.convService
    }
}
