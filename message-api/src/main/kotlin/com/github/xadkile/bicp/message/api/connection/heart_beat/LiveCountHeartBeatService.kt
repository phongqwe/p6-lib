package com.github.xadkile.bicp.message.api.connection.heart_beat

import com.github.michaelbull.result.Ok
import com.github.xadkile.bicp.message.api.connection.ipython_context.SocketProvider
import org.zeromq.ZContext
import org.zeromq.ZMQ
import kotlin.concurrent.thread

/**
 * Must not invoke the constructor directly unless in testing. An instance of this is provided by [IPythonContext].
 * This service is non-recoverable. If it detects a dead signal, it will remain dead, even when the service is back.
 * Due to its non-recoverable nature, it should only be bound and used along an IPython context that control its (the hb service)'s life cycle from creation, to stop.
 * TODO socket should be created within service thread, and closed within the service thread too.
 */
internal class LiveCountHeartBeatService constructor(
    zContext: ZContext,
    private val socketProvider: SocketProvider,
    liveCount: Int = 3,
    pollTimeout: Long = 1000,
) : AbstractLiveCountHeartBeatService(zContext, liveCount, pollTimeout) {

    private val convService = HeartBeatServiceConvImp(this)

    /**
     * init resources and start service thread
     */
    override fun start(): Boolean {
        if (!this.isServiceRunning()) {
            this.letThreadRunning = true
            this.serviceThread = thread(
                start = true,
                isDaemon = true) {
                val thisObj = this@LiveCountHeartBeatService
                val poller = zContext.createPoller(1)
                val socket = this.socketProvider.heartBeatSocket()
                poller.register(socket, ZMQ.Poller.POLLIN)
                poller.use {
                    while (letThreadRunning) {
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
