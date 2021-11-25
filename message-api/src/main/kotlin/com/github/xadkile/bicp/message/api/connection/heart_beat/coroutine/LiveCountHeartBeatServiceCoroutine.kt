package com.github.xadkile.bicp.message.api.connection.heart_beat.coroutine

import com.github.michaelbull.result.Ok
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatServiceConv
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatServiceConvImp
import com.github.xadkile.bicp.message.api.connection.ipython_context.SocketProvider
import kotlinx.coroutines.*
import org.zeromq.ZContext
import org.zeromq.ZMQ

/**
 * Must not invoke the constructor directly unless in testing. An instance of this is provided by [IPythonContext].
 * This service is non-recoverable. If it detects a dead signal, it will remain dead, even when the service is back.
 * Due to its non-recoverable nature, it should only be bound and used along an IPython context that control its (the hb service)'s life cycle from creation, to stop.
 *
 * This implementation is exactly like LiveCountHeartBeatServiceThread, but run on coroutine instead of thread
 */
internal class LiveCountHeartBeatServiceCoroutine constructor(
    zContext: ZContext,
    private val socketProvider: SocketProvider,
    liveCount: Int = 3,
    pollTimeout: Long = 1000,
    cScope: CoroutineScope,
    cDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : AbstractLiveCountHeartBeatServiceCoroutine(zContext, liveCount, pollTimeout, cScope, cDispatcher) {

    private val convService = HeartBeatServiceConvImp(this)

    /**
     * init resources and start service thread
     */
    override fun start(): Boolean {
        if (!this.isServiceRunning()) {
            this.job = cScope.launch(cDispatcher) {
                val socket = socketProvider.heartBeatSocket()
                socket.use { sk->
                    val poller = zContext.createPoller(1)
                    poller.register(sk, ZMQ.Poller.POLLIN)
                    poller.use {
                        while (isActive) {
                            val isAlive: Boolean = check(poller, sk) is Ok
                            if (isAlive) {
                                currentLives = liveCount
                            } else {
                                // rmd: only reduce life if there are lives left to prevent underflow of int
                                if (currentLives > 0) {
                                    currentLives -= 1
                                }
                            }
                        }
                    }
                }
            }
        }
        return true
    }

    override fun conv(): HeartBeatServiceConv {
        return this.convService
    }
}
