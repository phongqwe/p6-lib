package com.github.xadkile.bicp.message.api.connection.heart_beat

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.connection.ipython_context.SocketProvider
import com.github.xadkile.bicp.message.api.exception.UnknownException
import org.zeromq.ZContext
import org.zeromq.ZMQ

/**
 * Not for external use
 */
internal abstract class AbstractLiveCountHeartBeatService constructor(
    protected val zContext: ZContext,
    protected val liveCount: Int = 3,
    protected val pollTimeOut: Long = 1000,
) : HeartBeatService {

    protected var serviceThread: Thread? = null
    protected var currentLives:Int = 0
    protected var letThreadRunning:Boolean = false

    companion object {
        private val hbServiceNotRunningException = HeartBeatService.NotRunningException("[${this.hashCode()}] is not running")
    }

    override fun isHBAlive(): Boolean {
        if (this.isServiceRunning()) {
            return this.currentLives > 0
        } else {
            throw hbServiceNotRunningException
        }
    }

    override fun isServiceRunning(): Boolean {
        return this.serviceThread?.isAlive ?: false
    }

    protected fun check(poller: ZMQ.Poller, socket: ZMQ.Socket): Result<Unit,Exception> {
        try{
            socket.send("a".toByteArray())
            poller.poll(this.pollTimeOut)
            val o = poller.pollin(0)
            if (o) {
                val output = socket.recv()
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
        if(this.isServiceRunning()){
            if (this.serviceThread != null && this.serviceThread?.isAlive == true) {
                // rmd: this signal the thread to stop. The thread will stop in its next iteration
                this.letThreadRunning = false
                // rmd: wait until the thread is stopped completely
                while(this.serviceThread?.isAlive == true){
                    Thread.sleep(50)
                }
                this.serviceThread = null
            }
        }
        return true
    }
}
