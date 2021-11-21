package com.github.xadkile.bicp.message.api.connection.heart_beat

import com.github.michaelbull.result.Ok
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.util.*
import kotlin.concurrent.thread


/**
 * This service can exist independently from [IPythonContext]
 */
internal class LiveCountHeartBeatServiceUpdatable constructor(
    zContext: ZContext,
    hbSocket: ZMQ.Socket,
    liveCount: Int = 3,
    pollTimeout: Long = 1000,
) : HeartBeatServiceUpdatable,
    AbstractLiveCountHeartBeatService(zContext, hbSocket, liveCount,pollTimeout) {

    private val convService = HeartBeatServiceConvImp(this)
    private val updateEventList: Queue<UpdateEvent> = ArrayDeque()

    override fun updateSocket(newSocket: ZMQ.Socket) {
        updateEventList.offer {
            this.hbSocket = newSocket
            UpdateSignal.UPDATE_SOCKET
        }
    }

    override fun subscribe(updater: HeartBeatServiceUpdater) {
        TODO("Not yet implemented")
    }

    /**
     * init resources and start service thread
     */
    override fun start(): Boolean {
        this.letThreadRunning = true
        this.serviceThread = thread(true) {
            val thisObj = this@LiveCountHeartBeatServiceUpdatable
            var poller = zContext.createPoller(1)
            poller.use {
                poller.register(this.hbSocket)
                while (letThreadRunning) {
                    // rmd: consume update events before doing anything
                    while (this.updateEventList.isNotEmpty()) {
                        val event:UpdateEvent = this.updateEventList.poll()
                        val updateSignal:UpdateSignal = event.consum()
                        when (updateSignal) {
                            UpdateSignal.UPDATE_SOCKET -> {
                                poller.close()
                                poller = zContext.createPoller(1)
                                poller.register(this.hbSocket)
                            }
                            else -> {
                            }
                        }
                    }

                    val isAlive: Boolean = thisObj.check(poller, hbSocket) is Ok
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
