package com.github.xadkile.bicp.message.api.connection.heart_beat.thread

import com.github.michaelbull.result.Ok
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatServiceConv
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatServiceConvImp
import com.github.xadkile.bicp.message.api.connection.ipython_context.SocketProvider
import org.zeromq.ZContext
import org.zeromq.ZMQ
import kotlin.concurrent.thread

/**
 * Must not invoke the constructor directly unless in testing. An instance of this is provided by [IPythonContext].
 * This service is non-recoverable. If it detects a dead signal, it will remain dead, even when the service is back.
 * Due to its non-recoverable nature, it should only be bound and used along an IPython context that control its (the hb service)'s life cycle from creation, to stop.
 *
 * This implementation is exactly like LiveCountHeartBeatServiceCoroutine, but run on thread instead of coroutine
 */
internal class LiveCountHeartBeatServiceThread constructor(
    zContext: ZContext,
    private val socketProvider: SocketProvider,
    liveCount: Int = 3,
    pollTimeout: Long = 1000,
) : AbstractLiveCountHeartBeatServiceThread(zContext, liveCount, pollTimeout) {

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
                val thisObj = this@LiveCountHeartBeatServiceThread
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
