package com.github.xadkile.bicp.message.api.connection.service.heart_beat.coroutine

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.connection.service.heart_beat.exception.HBServiceNotRunningCrashException
import com.github.xadkile.bicp.message.api.connection.service.heart_beat.HeartBeatService
import com.github.xadkile.bicp.exception.ExceptionInfo
import com.github.xadkile.bicp.exception.UnknownException
import kotlinx.coroutines.*
import org.zeromq.ZContext
import org.zeromq.ZMQ

/**
 * Not for external use
 */
internal sealed class AbstractLiveCountHeartBeatServiceCoroutine constructor(
    protected val zContext: ZContext,
    protected val liveCount: Int = 3,
    private val pollTimeOut: Long = 1000,
    protected val cScope: CoroutineScope,
    protected val cDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : HeartBeatService {

    protected var currentLives:Int = 0
    protected var job:Job?=null

    /**
     * Justification for throwing an exception here: the programmer must make sure this function must not be called if the service is not running.
     */
    override fun isHBAlive(): Boolean {
        if (this.isServiceRunning()) {
            return this.currentLives > 0
        } else {
            throw HBServiceNotRunningCrashException()
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
                val output = hbSocket.recv(ZMQ.DONTWAIT)
                if(output != null){
                    return Ok(Unit)
                }else{
                    return Err(UnknownException("output of heartbeat zmq channel is null"))
                }
            } else {
                return Err(UnknownException(
                    ExceptionInfo(
                        msg ="not receiving hb signal",
                        data = o,
                        loc =this
                    )
                ))
            }
        }catch (e:Exception){
            return Err(e)
        }
    }

    /**
     * Stop the service thread and cleaning up resources.
     */
    override suspend fun stop(): Result<Unit,Exception> {
        // rmd: runBlocking so that all the suspending functions are completed before returning,
        // rmd: guaranteeing that this service is completely stopped when this function returns.
        if(this.isServiceRunning()){
            this.bluntStop()
        }
        return Ok(Unit)
    }

    protected suspend fun bluntStop(){
        job?.cancelAndJoin()
        this.job = null
    }
}
