package com.github.xadkile.bicp.message.api.connection.heart_beat

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.exception.UnknownException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.zeromq.ZContext
import org.zeromq.ZMQ
import kotlin.concurrent.thread

/**
 * Must not invoke the constructor directly unless in testing. An instance of this is provided by [IPythonContext].
 * This service is non-recoverable. If it detects a dead signal, it will remain dead, even when the service is back.
 * [interval] is waiting period between each heart beat check.
 */
internal class LiveCountHeartBeatService constructor(
    zContext: ZContext,
    hbSocket: ZMQ.Socket,
    liveCount: Int = 3,
    interval: Long = 1000,
    socketTimeOut: Long = 1000,
) : HeartBeatService
    ,AbstractLiveCountHeartBeatService(zContext, hbSocket, liveCount, interval,socketTimeOut) {

    private val convService = HeartBeatServiceConvImp(this)

    /**
     * init resources and start service thread
     */
    override fun start(): Boolean {
        if(!this.isServiceRunning()){
            this.letThreadRunning = true
            this.serviceThread = thread(true) {
                val thisObj = this@LiveCountHeartBeatService
                val poller = zContext.createPoller(1)
                poller.use {
                    poller.register(this.hbSocket)
                    while (letThreadRunning) {
                        val isAlive: Boolean = thisObj.check(poller, hbSocket) is Ok
                        if (isAlive) {
                            this.currentLives = this.liveCount
                        } else {
                            // rmd: only reduce life if there are lives left to prevent underflow of int
                            if(this.currentLives > 0){
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

    /**
     * for testing only
     */
    internal fun getInterval():Long{
        return this.interval
    }
}
