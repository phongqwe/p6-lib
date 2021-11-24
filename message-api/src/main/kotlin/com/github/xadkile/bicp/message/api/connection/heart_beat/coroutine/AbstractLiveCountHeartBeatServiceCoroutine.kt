package com.github.xadkile.bicp.message.api.connection.heart_beat.coroutine

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatService
import com.github.xadkile.bicp.message.api.exception.UnknownException
import kotlinx.coroutines.*
import org.zeromq.ZContext
import org.zeromq.ZMQ

/**
 * Not for external use
 */
internal sealed class AbstractLiveCountHeartBeatServiceCoroutine constructor(
    protected val zContext: ZContext,
    protected val liveCount: Int = 3,
    protected val pollTimeOut: Long = 1000,
    protected val cScope: CoroutineScope,
    protected val cDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : HeartBeatService {

    protected var currentLives:Int = 0
    protected var job:Job?=null

    companion object {
        private val hbServiceNotRunningException =
            HeartBeatService.NotRunningException("[${this.hashCode()}] is not running")
    }

    override fun isHBAlive(): Boolean {
        if (this.isServiceRunning()) {
            return this.currentLives > 0
        } else {
            throw hbServiceNotRunningException
        }
    }

    override fun isServiceRunning(): Boolean {
        return this.job?.isActive ?: false
    }

    /**
     * Send a ping msg to [hbSocket] to see if it is running.
     * [poller] has already registered [hbSocket].
     */
    protected fun check(poller: ZMQ.Poller, hbSocket: ZMQ.Socket): Result<Unit,Exception> {
        try{
            hbSocket.send("a".toByteArray())
            poller.poll(this.pollTimeOut)
            val o = poller.pollin(0)
            if (o) {
                val output = hbSocket.recv()
                if(output != null){
                    return Ok(Unit)
                }else{
                    return Err(UnknownException("output of heartbeat channel is null"))
                }
            } else {
                return Err(UnknownException("impossible heart beat poller result: more than 1 "))
            }
        }catch (e:Exception){
            return Err(e)
        }
    }

    /**
     * Stop the service thread and cleaning up resources.
     */
    override fun stop(): Boolean {
        // rmd: runBlocking so that all the suspending functions are completed before returning,
        // rmd: guaranteeing that this service is completely stopped when this function returns.
        runBlocking {
            job?.cancelAndJoin()
        }
        this.job = null
        return true
    }
}
