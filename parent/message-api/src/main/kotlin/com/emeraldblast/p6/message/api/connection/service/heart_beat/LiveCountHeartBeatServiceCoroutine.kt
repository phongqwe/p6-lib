package com.emeraldblast.p6.message.api.connection.service.heart_beat

import com.github.michaelbull.result.*
import com.emeraldblast.p6.common.exception.error.CommonErrors
import com.emeraldblast.p6.common.exception.error.ErrorReport
import com.emeraldblast.p6.message.api.connection.kernel_context.KernelContext
import com.emeraldblast.p6.message.api.connection.kernel_context.KernelCoroutineScope
import com.emeraldblast.p6.message.api.connection.service.heart_beat.errors.HBServiceCrashException
import com.emeraldblast.p6.message.api.connection.service.heart_beat.errors.HBServiceErrors
import com.emeraldblast.p6.message.api.other.Sleeper
import com.emeraldblast.p6.message.di.ServiceCoroutineDispatcher
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import org.zeromq.ZMQ

/**
 * Must not invoke the constructor directly unless in testing. An instance of this is provided by [IPythonContext].
 * This service is recoverable. If a kernel is restarted while this service is running, this service will still able to detect heart-beat channel when the kernel is started (with new hb channel on new port)
 */
class LiveCountHeartBeatServiceCoroutine @AssistedInject constructor(
    @Assisted private val kernelContext: KernelContext,
    @Assisted private val liveCount: Int = 20,
    @Assisted("pollTimeOut") private val pollTimeOut: Long = 1_000,
    @Assisted("startTimeOut") private val startTimeOut: Long = 50_000,
    @KernelCoroutineScope
    private val coroutineScope: CoroutineScope,
    @ServiceCoroutineDispatcher
    private val dispatcher: CoroutineDispatcher,
) : HeartBeatService {


    /**
     * init resources and start service thread
     */
    override suspend fun start(): Result<Unit, ErrorReport> {
        if (this.isServiceRunning()) {
            return Ok(Unit)
        }
        var skipErr: ErrorReport? = null

        this.job = coroutineScope.launch(dispatcher) {
            var needReConnect = false
            val socketRs = kernelContext.getSocketProvider().map { it.heartBeatSocket() }
            if (socketRs is Ok) {
                var socket = socketRs.value
                var poller = kernelContext.zContext().createPoller(1)
                poller.register(socket, ZMQ.Poller.POLLIN)
                poller.use {
                    while (isActive) {
                        val checkRs = this@LiveCountHeartBeatServiceCoroutine.check(poller, socket)
                        if (checkRs is Ok) {
                            currentLives = liveCount
                            needReConnect = false
                        } else {
                            // rmd: only reduce life if there are lives left
                            if (currentLives > 0) {
                                currentLives -= 1
                            }
                            if (currentLives <= 0) {
                                needReConnect = true
                            }
                        }
                        if (needReConnect) {
                            // attempt to re-reconnect to new heartbeat channel
                            poller.close()
                            val socketRs2 = kernelContext.getSocketProvider().map { it.heartBeatSocket() }
                            if (socketRs2 is Ok) {
                                socket = socketRs2.value
                                poller = kernelContext.zContext().createPoller(1)
                                poller.register(socket, ZMQ.Poller.POLLIN)
                            }
                        }
                    }
                }
            } else {
                skipErr = socketRs.getError()
                this.cancel()
            }
        }
        if (skipErr != null) {
            return Err(skipErr as ErrorReport)
        }
        val rt = this.waitTillLive()
        if (rt is Err) {
            job?.cancel()
            val report = ErrorReport(
                header = CommonErrors.TimeOut.header,
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
                header = CommonErrors.TimeOut.header,
                data = CommonErrors.TimeOut.Data("Time out when trying to start heart beat service"),
            )
        }

        if (rt is Err) {
            return rt
        }

        val waitRs2 = Sleeper.delayUntil(50, startTimeOut) { this.isHBAlive() }

        val rt2 = waitRs2.mapError {
            ErrorReport(
                header = HBServiceErrors.CantStartHBService.header,
                data = HBServiceErrors.CantStartHBService.Data("Time out when waiting for HB to come live"),
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
            hbSocket.send(byteArrayOf('a'.code.toByte()))
            poller.poll(pollTimeOut)
            val o = poller.pollin(0)
            if (o) {
                val output = hbSocket.recv()
                if (output != null) {
                    return Ok(Unit)
                } else {
                    val report = ErrorReport(
                        header = CommonErrors.Unknown.header,
                        data = CommonErrors.Unknown.Data("output of heartbeat zmq channel is null", null)
                    )
                    return Err(report)
                }
            } else {
                val report = ErrorReport(
                    header = CommonErrors.Unknown.header,
                    data = CommonErrors.Unknown.Data("not receiving heart beat signal", null)
                )
                return Err(report)
            }
        } catch (e: Exception) {
            val report = CommonErrors.ExceptionError.report(e)
            return Err(report)
        }
    }

    override suspend fun stopJoin(): Result<Unit, ErrorReport> {
        if (this.isServiceRunning()) {
            this.bluntJoinStop()
        }
        return Ok(Unit)
    }

    override fun stop(): Result<Unit, ErrorReport> {
        if (this.isServiceRunning()) {
            this.bluntStop()
        }
        return Ok(Unit)
    }

    private fun bluntStop() {
        job?.cancel()
    }

    private suspend fun bluntJoinStop() {
        job?.cancelAndJoin()
    }
}
