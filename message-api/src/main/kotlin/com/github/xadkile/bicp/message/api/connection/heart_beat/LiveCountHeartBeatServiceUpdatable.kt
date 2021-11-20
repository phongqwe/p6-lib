package com.github.xadkile.bicp.message.api.connection.heart_beat

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.exception.UnknownException
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.util.*
import kotlin.concurrent.thread


enum class UpdateSignal {
    UPDATE_SOCKET, UPDATE_LIVE_COUNT
}

fun interface UpdateEvent {
    fun consum(): UpdateSignal
}

/**
 * Must not invoke the constructor directly unless in testing. An instance of this is provided by [IPythonContext].
 * [interval] is waiting period between each heart beat check.
 */
internal class LiveCountHeartBeatServiceUpdatable constructor(
    zContext: ZContext,
    hbSocket: ZMQ.Socket,
    liveCount: Int = 3,
    interval: Long = 1000,
    pollTimeout: Long = 1000,
) : HeartBeatServiceUpdatable
    ,AbstractLiveCountHeartBeatService(zContext, hbSocket, liveCount, interval,pollTimeout) {
//{

//    private var serviceThread: Thread? = null
//    private var currentLives: Int = 0
//    private var letThreadRunning: Boolean = false
    private val convService = HeartBeatServiceConvImp(this)
    private val updateEventList: Queue<UpdateEvent> = ArrayDeque()

//    companion object {
//        private val hbServiceNotRunningException =
//            HeartBeatService.NotRunningException("[${this.hashCode()}] is not running")
//    }

    override fun updateSocket(newSocket: ZMQ.Socket) {
        updateEventList.offer {
            this.hbSocket = newSocket
            UpdateSignal.UPDATE_SOCKET
        }
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

//    override fun isHBAlive(): Boolean {
//        if (this.isServiceRunning()) {
//            return this.currentLives > 0 //1 2 3
//        } else {
//            throw hbServiceNotRunningException
//        }
//    }
//
//    override fun isServiceRunning(): Boolean {
//        return this.serviceThread?.isAlive ?: false
//    }
//
//    private fun check(poller: ZMQ.Poller, socket: ZMQ.Socket): Result<Unit,Exception> {
//        try{
//            socket.send("a".toByteArray())
//            val i: Int = poller.poll(this.pollTimeout)
//            if (i == 1) {
//                val output = socket.recv()
//                if(output != null){
//                    return Ok(Unit)
//                }else{
//                    return Err(UnknownException("output of heartbeat channel is null"))
//                }
//            } else {
//                return Err(UnknownException("impossible heart beat poller result: more than 1 "))
//            }
//        }catch (e:Exception){
//            return Err(e)
//        }
//
//    }

//    override fun checkHB(): Result<Unit, Exception> {
//        if (this.isServiceRunning() && this.zContext.isClosed.not()) {
//            try {
//                val poller: ZMQ.Poller = zContext.createPoller(1)
//                poller.register(this.hbSocket, ZMQ.Poller.POLLIN)
//                val rt = this.check(poller, this.hbSocket)
//                poller.close()
//                return rt
//            } catch (e: Exception) {
//                return Err(e)
//            }
//        } else {
//            return Err(hbServiceNotRunningException)
//        }
//    }
//
//    /**
//     * Stop the service thread and cleaning up resources.
//     */
//    override fun stop(): Boolean {
//        if (this.isServiceRunning()) {
//            if (this.serviceThread != null && this.serviceThread?.isAlive == true) {
//                // rmd: this signal the thread to stop. The thread will stop in its next iteration
//                this.letThreadRunning = false
//                // rmd: wait until the thread is stop completely
//                while(this.serviceThread!=null && this.serviceThread?.isAlive == true){
//                    Thread.sleep(100)
//                }
//                this.serviceThread = null
//            }
//        }
//        return true
//    }

    override fun conv(): HeartBeatServiceConv {
        return this.convService
    }

    /**
     * for testing only
     */
    internal fun getInterval(): Long {
        return this.interval
    }
}
