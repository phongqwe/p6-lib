package com.github.xadkile.bicp.message.api.connection.heart_beat

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.exception.UnknownException
import org.zeromq.ZContext
import org.zeromq.ZMQ

internal abstract class AbstractLiveCountHeartBeatService constructor(
    protected val zContext: ZContext,
    protected var hbSocket: ZMQ.Socket,
    protected val liveCount: Int = 3,
    protected val interval: Long = 1000,
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
            val i: Int = poller.poll(this.pollTimeOut)
            if (i == 1) {
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
     * TODO this method should be discarded
     * Calling it while the service thread is running very likely will return an exception.
     * The reason is:
     * Heart beat channel is a REP channel. After sending a message, I must call recv, before making another send.
     * Failing to recv will leave the socket in error state.
     * Because the service thread is a forever loop, it will do send-recv non stop. If the call checkHB accidently happends right after a "send" in service thread. That will causes an exception.
     */
    private fun checkHB(): Result<Unit,Exception> {
        if (this.isServiceRunning() && this.zContext.isClosed.not()) {
            try{
                val poller: ZMQ.Poller = zContext.createPoller(1)
                poller.register(this.hbSocket, ZMQ.Poller.POLLIN)
                val rt = this.check(poller, this.hbSocket)
                poller.close()
                return rt
            }catch (e:Exception){
                return Err(e)
            }
        } else {
            return Err(hbServiceNotRunningException)
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
