package com.github.xadkile.p6.message.api.connection.service.heart_beat

import com.github.michaelbull.result.*
import com.github.xadkile.p6.common.exception.error.CommonErrors
import com.github.xadkile.p6.common.exception.error.ErrorReport
import com.github.xadkile.p6.message.api.connection.kernel_context.KernelContext
import com.github.xadkile.p6.message.api.connection.kernel_context.context_object.SocketFactory
import com.github.xadkile.p6.message.api.connection.service.heart_beat.errors.HBServiceCrashException
import com.github.xadkile.p6.message.api.connection.service.heart_beat.errors.HBServiceErrors
import com.github.xadkile.p6.message.api.other.Sleeper
import kotlinx.coroutines.*
import org.zeromq.ZContext
import org.zeromq.ZMQ

/**
 * Must not invoke the constructor directly unless in testing. An instance of this is provided by [IPythonContext].
 * This service is recoverable. If a kernel is restarted while this service is running, this service will still able to detect heart-beat channel when the kernel is started (with new hb channel on new port)
 */
internal class LiveCountHeartBeatServiceCoroutine constructor(
    private val kernelContext:KernelContext,
    private val liveCount: Int = 20,
    private val pollTimeOut: Long = 1_000,
    private val startTimeOut: Long = 50_000,
    private val cScope: CoroutineScope,
    private val cDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : HeartBeatService {


    /**
     * init resources and start service thread
     */
    override suspend fun start(): Result<Unit, ErrorReport> {
        if (this.isServiceRunning()) {
            return Ok(Unit)
        }
        var skipErr:ErrorReport?= null

        this.job = cScope.launch(cDispatcher) {
            var needReConnect = false
            val socketRs = kernelContext.getSocketProvider().map { it.heartBeatSocket() }
            if(socketRs is Ok){
                var socket = socketRs.value
                var poller = kernelContext.zContext().createPoller(1)
                poller.register(socket, ZMQ.Poller.POLLIN)
                poller.use {
                    while (isActive) {
                        val checkRs = check(poller, socket)
                        if (checkRs is Ok) {
                            currentLives = liveCount
                            needReConnect = false
                        } else {
                            // rmd: only reduce life if there are lives left
                            if (currentLives > 0) {
                                currentLives -= 1
                            }
                            if(currentLives <=0){
                                needReConnect = true
                            }
                        }
                        if(needReConnect){
                            // attempt to re-reconnect to new heartbeat channel
                            socket.close()
                            poller.close()
                            val socketRs2 = kernelContext.getSocketProvider().map { it.heartBeatSocket() }
                            if(socketRs2 is Ok){
                                socket = socketRs2.value
                                poller = kernelContext.zContext().createPoller(1)
                                poller.register(socket, ZMQ.Poller.POLLIN)
                            }
                        }
                    }
                }
                socket.close()
            }else{
                skipErr= socketRs.getError()
              this.cancel()
            }
        }
        if(skipErr!=null){
            return Err(skipErr as ErrorReport)
        }
        val rt = this.waitTillLive()
        if (rt is Err) {
            bluntStop()
            val report = ErrorReport(
                type = CommonErrors.TimeOut,
                data = CommonErrors.TimeOut.Data("Time out when trying to start IOPub service")
            )
            return Err(report)
        }
        return rt
    }

    /**
     * wait until this service goes live
     */
    private suspend fun waitTillLive(): Result<Unit, ErrorReport> {
        val waitRs = Sleeper.delayUntil(50, startTimeOut) { this.isRunning() }
        val loc = "${this.javaClass.canonicalName}:waitTillLive"
        val rt = waitRs.mapError {
            ErrorReport(
                type = CommonErrors.TimeOut,
                data = CommonErrors.TimeOut.Data("Time out when trying to start heart beat service"),
                loc = loc
            )
        }

        if (rt is Err) {
            return rt
        }

        val waitRs2 = Sleeper.delayUntil(50, startTimeOut) { this.isHBAlive() }

        val rt2 = waitRs2.mapError {
            ErrorReport(
                type = HBServiceErrors.CantStartHBService,
                data = HBServiceErrors.CantStartHBService.Data("Time out when waiting for HB to come live"),
                loc = loc
            )
        }
        return rt2
    }

    protected var currentLives: Int = 0
    protected var job: Job? = null

    /**
     * Justification for throwing an exception here: the programmer must make sure this function must not be called if the service is not running.
     */
    override fun isHBAlive(): Boolean {
        if (this.isServiceRunning()) {
            return this.currentLives > 0
        } else {
            throw HBServiceCrashException()
        }
    }

    override fun isServiceRunning(): Boolean {
        return this.job?.isActive ?: false
    }

    /**
     * Send a ping msg to [hbSocket] to see if it is running.
     * [poller] has already registered [hbSocket].
     */
    private fun check(poller: ZMQ.Poller, hbSocket: ZMQ.Socket): Result<Unit, ErrorReport> {
        try {
            hbSocket.send("a".toByteArray())
            poller.poll(pollTimeOut)
            val o = poller.pollin(0)
            if (o) {
                val output = hbSocket.recv(ZMQ.DONTWAIT)
                if (output != null) {
                    return Ok(Unit)
                } else {
                    val report = ErrorReport(
                        type = CommonErrors.Unknown,
                        data = CommonErrors.Unknown.Data("output of heartbeat zmq channel is null", null)
                    )
                    return Err(report)
                }
            } else {
                val report = ErrorReport(
                    type = CommonErrors.Unknown,
                    data = CommonErrors.Unknown.Data("not receiving heart beat signal", null)
                )
                return Err(report)
            }
        } catch (e: Exception) {
            val report = ErrorReport(
                type = CommonErrors.ExceptionError,
                data = CommonErrors.ExceptionError.Data(e)
            )
            return Err(report)
        }
    }

    /**
     * Stop the service thread and cleaning up resources.
     */
    override suspend fun stop(): Result<Unit, ErrorReport> {
        // rmd: runBlocking so that all the suspending functions are completed before returning,
        // rmd: guaranteeing that this service is completely stopped when this function returns.
        if (this.isServiceRunning()) {
            this.bluntStop()
        }
        return Ok(Unit)
    }

    private suspend fun bluntStop() {
        job?.cancelAndJoin()
        this.job = null
    }
}
