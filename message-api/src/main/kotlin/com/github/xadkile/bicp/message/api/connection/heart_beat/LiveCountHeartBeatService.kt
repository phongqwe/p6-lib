package com.github.xadkile.bicp.message.api.connection.heart_beat

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.exception.UnknownException
import org.zeromq.ZContext
import org.zeromq.ZMQ
import kotlin.concurrent.thread

/**
 * Must not invoke the constructor directly unless in testing. An instance of this is provided by [IPythonContext]
 */
internal class LiveCountHeartBeatService constructor(
    private val zContext: ZContext,
    private val hbSocket: ZMQ.Socket,
    private val liveCount: Int = 3,
    private val interval: Long = 1000,
    private val socketTimeOut: Long = 1000,
) : HeartBeatService {

    private var hbLiveness: Boolean = false
    private var serviceThread: Thread? = null
    private var poller: ZMQ.Poller? = null
    private var isRunning: Boolean = false

    private val convService = HeartBeatServiceConvImp(this)

    companion object {
        private val hbServiceNotRunningException = HeartBeatService.NotRunningException("[${this.hashCode()}] is not running")
    }

    /**
     * init resources and start service thread
     */
    override fun start(): Boolean {
        this.poller = zContext.createPoller(1)
        this.poller?.register(this.hbSocket)
        this.serviceThread = thread(true) {
            val thisObj = this@LiveCountHeartBeatService
            var whileCond = true
            var currentLives: Int = liveCount
            // rmd: continue looping if the hb channel is alive or there's live count left
            while (whileCond || currentLives > 0) {
                val res: Boolean = thisObj.check(poller!!, hbSocket)
                thisObj.hbLiveness = res
                whileCond = res
                if (!res) {
                    currentLives -= 1
                } else {
                    // restore lives if it is still alive
                    currentLives = liveCount
                }
                try {
                    Thread.sleep(interval)
                } catch (e: Exception) {
                    break
                    // ignore
                }
            }
        }
        this.isRunning = true
        return true
    }

    internal fun getThread(): Thread? {
        return this.serviceThread
    }

    override fun isHBAlive(): Boolean {
        if (this.isServiceRunning()) {
            return this.hbLiveness
        } else {
            throw hbServiceNotRunningException
        }
    }

    override fun isServiceRunning(): Boolean {
        return this.isRunning
    }

    private fun check(poller: ZMQ.Poller, socket: ZMQ.Socket): Boolean {
        socket.send("a".toByteArray())
        val i: Int = poller.poll(this.socketTimeOut)
        if (i == 1) {
            val output = socket.recv()
            return output != null
        } else {
            return false
        }
    }

    override fun checkHB(): Result<Unit,Exception> {
        if (this.isRunning && this.zContext.isClosed.not()) {
            try{
                val poller: ZMQ.Poller = zContext.createPoller(1)
                poller.register(this.hbSocket, ZMQ.Poller.POLLIN)
                val rt = this.check(poller, this.hbSocket)
                poller.close()
                if(rt){
                    return Ok(Unit)
                }else{
                    return Err(UnknownException(""))
                }
            }catch (e:Exception){
                return Err(e)
            }
        } else {
            return Err(hbServiceNotRunningException)
        }
    }

    /**
     * cleaning resources
     */
    override fun stop(): Boolean {
        if(this.isRunning){
            if (this.serviceThread != null && this.serviceThread?.isAlive == true) {
                try {
                    this.serviceThread?.interrupt()
                } catch (e: Exception) {
                    // ignore
                } finally {
                    this.serviceThread = null
                }
                poller?.close()
                this.poller = null
            }
        }
        this.isRunning = false
        return true
    }

    override fun conv(): HeartBeatServiceConv {
        return this.convService
    }
}
