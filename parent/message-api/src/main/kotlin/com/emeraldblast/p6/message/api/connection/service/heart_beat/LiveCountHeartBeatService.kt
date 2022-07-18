package com.emeraldblast.p6.message.api.connection.service.heart_beat

import com.github.michaelbull.result.*
import com.emeraldblast.p6.common.exception.error.CommonErrors
import com.emeraldblast.p6.common.exception.error.ErrorReport
import com.emeraldblast.p6.message.api.connection.kernel_context.KernelContext
import com.emeraldblast.p6.message.api.connection.kernel_context.KernelCoroutineScope
import com.emeraldblast.p6.message.api.connection.service.heart_beat.errors.HBServiceCrashException
import com.emeraldblast.p6.message.di.ServiceCoroutineDispatcher
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import org.zeromq.ZMQ
import kotlin.properties.Delegates

/**
 * This service is recoverable. If a kernel is restarted while this service is running, this service will still able to detect heart-beat channel when the kernel is started (with new hb channel on new port)
 */
class LiveCountHeartBeatService @AssistedInject constructor(
    @Assisted private val kernelContext: KernelContext,
    @Assisted private val liveCount: Int = 20,
    @Assisted("pollTimeOut") private val pollTimeOut: Long = 1_000,
    @Assisted("startTimeOut") private val startTimeOut: Long = 50_000,
    @KernelCoroutineScope
    private val coroutineScopeX: CoroutineScope,
    @ServiceCoroutineDispatcher
    private val dispatcher: CoroutineDispatcher,
) : HeartBeatService {


    private val socketProviderRs get()= kernelContext.getSocketProvider()
    private val zContext get()=kernelContext.zContext()
    /**
     * when hb is not-alive/dead => uncompleted job
     * when hb is alive => completed job
     */
    private var hbLiveWaitJob: CompletableDeferred<Result<Unit, ErrorReport>> = CompletableDeferred()

    private var currentLives by Delegates.observable(0, onChange = { property, oldValue, newValue ->
        if (newValue == 0) {
            hbLiveWaitJob = CompletableDeferred()
        } else {
            if (hbLiveWaitJob.isActive) {
                hbLiveWaitJob.complete(Ok(Unit))
            }
        }
    })
    private var job: Job? = null

    override suspend fun start(): Result<Unit, ErrorReport> {
        if (this.isServiceRunning()) {
            return Ok(Unit)
        }

        var poller = zContext.createPoller(1)
        val socketRs = socketProviderRs.map { it.heartBeatSocket() }
        var startErr: ErrorReport? = null
        val sv = this
        try {
            withTimeout(startTimeOut) {
                sv.job = coroutineScopeX.launch(dispatcher) {
                    var needReConnect = false
                    if (socketRs is Ok) {
                        var socket = socketRs.value
                        poller.register(socket, ZMQ.Poller.POLLIN)
                        poller.use {
                            while (isActive) {
                                val checkRs = this@LiveCountHeartBeatService.check(poller, socket)
                                if (checkRs is Ok) {
                                    currentLives = liveCount
                                    needReConnect = false
                                } else {
                                    // x: only reduce life if there are lives left
                                    if (currentLives > 0) {
                                        currentLives -= 1
                                    }
                                    // x: when current live drop to 0 => python side is presumed dead => prepare to reconnect
                                    if (currentLives <= 0) {
                                        needReConnect = true
                                    }
                                }
                                if (needReConnect) {
                                    // attempt to re-reconnect to new heartbeat channel
                                    poller.close()
                                    val socketRs2 = socketProviderRs.map { it.heartBeatSocket() }
                                    if (socketRs2 is Ok) {
                                        socket = socketRs2.value
                                        poller = zContext.createPoller(1)
                                        poller.register(socket, ZMQ.Poller.POLLIN)
                                    }
                                }
                            }
                        }
                    } else {
                        startErr = socketRs.getError()
                        this.cancel()
                    }
                }
            }
        } catch (e: Exception) {
            job?.cancel()
            return CommonErrors.TimeOut.report("timeOutMessage").toErr()
        }
        if (startErr != null) {
            return startErr!!.toErr()
        }
        val waitHbLive = this.waitHBALive()
        return waitHbLive
    }


//    override suspend fun start(): Result<Unit, ErrorReport> {
//        if (this.isServiceRunning()) {
//            return Ok(Unit)
//        }
//
//        var poller = kernelContext.zContext().createPoller(1)
//        val socketRs = kernelContext.getSocketProvider().map { it.heartBeatSocket() }
//        var startErr: ErrorReport? = null
//
//        val timeOutRs = Sleeper.delayUntil2(
//            timeOut = startTimeOut,
//            timeOutMessage = "Time out when trying to start heart beat service",
//            failSignal = { this.job == null },
//        ) { startTimeOutJob ->
//            this.job = coroutineScopeX.launch(dispatcher) {
//                var needReConnect = false
//                if (socketRs is Ok) {
//                    var socket = socketRs.value
//                    poller.register(socket, ZMQ.Poller.POLLIN)
//                    poller.use {
//                        while (isActive) {
////                            if (startTimeOutJob.isActive) {
////                                startTimeOutJob.complete(Ok(Unit))
////                            }
//                            val checkRs = this@LiveCountHeartBeatServiceCoroutine.check(poller, socket)
//                            if (checkRs is Ok) {
//                                currentLives = liveCount
//                                needReConnect = false
//                            } else {
//                                // rmd: only reduce life if there are lives left
//                                if (currentLives > 0) {
//                                    currentLives -= 1
//                                }
//                                if (currentLives <= 0) {
//                                    needReConnect = true
//                                }
//                            }
//                            if (needReConnect) {
//                                // attempt to re-reconnect to new heartbeat channel
//                                poller.close()
//                                val socketRs2 = kernelContext.getSocketProvider().map { it.heartBeatSocket() }
//                                if (socketRs2 is Ok) {
//                                    socket = socketRs2.value
//                                    poller = kernelContext.zContext().createPoller(1)
//                                    poller.register(socket, ZMQ.Poller.POLLIN)
//                                }
//                            }
//                        }
//                    }
//                } else {
//                    startErr = socketRs.getError()
//                    this.cancel()
//                }
//            }
//            if (startTimeOutJob.isActive) {
//                startTimeOutJob.complete(Ok(Unit))
//            }
//        }
//
//        if (startErr != null) {
//            return Err(startErr as ErrorReport)
//        }
//        val rt = timeOutRs
//        if (rt is Err) {
//            job?.cancel()
//            return Err(rt.error)
//        }
//        return rt
//    }

    /**
     * init resources and start service thread
     */
//    override suspend fun start(): Result<Unit, ErrorReport> {
//        if (this.isServiceRunning()) {
//            return Ok(Unit)
//        }
//
//        val startTimeOutJob: CompletableDeferred<Result<Unit, ErrorReport>> = CompletableDeferred()
//        coroutineScope.launch(dispatcher) {
//            delay(startTimeOut)
//            if (startTimeOutJob.isActive) {
//                startTimeOutJob.complete(
//                    CommonErrors.TimeOut
//                        .report("Time out when trying to start heart beat service")
//                        .toErr()
//                )
//            }
//        }
//
//        var poller = kernelContext.zContext().createPoller(1)
//        val socketRs = kernelContext.getSocketProvider().map { it.heartBeatSocket() }
//        var startErr: ErrorReport? = null
//
//        this.job = coroutineScope.launch(dispatcher) {
//            var needReConnect = false
//            if (socketRs is Ok) {
//                var socket = socketRs.value
//                poller.register(socket, ZMQ.Poller.POLLIN)
//                poller.use {
//                    while (isActive) {
//                        if (startTimeOutJob.isActive) {
//                            startTimeOutJob.complete(Ok(Unit))
//                        }
//                        val checkRs = this@LiveCountHeartBeatServiceCoroutine.check(poller, socket)
//                        if (checkRs is Ok) {
//                            currentLives = liveCount
//                            needReConnect = false
//                        } else {
//                            // rmd: only reduce life if there are lives left
//                            if (currentLives > 0) {
//                                currentLives -= 1
//                            }
//                            if (currentLives <= 0) {
//                                needReConnect = true
//                            }
//                        }
//                        if (needReConnect) {
//                            // attempt to re-reconnect to new heartbeat channel
//                            poller.close()
//                            val socketRs2 = kernelContext.getSocketProvider().map { it.heartBeatSocket() }
//                            if (socketRs2 is Ok) {
//                                socket = socketRs2.value
//                                poller = kernelContext.zContext().createPoller(1)
//                                poller.register(socket, ZMQ.Poller.POLLIN)
//                            }
//                        }
//                    }
//                }
//            } else {
//                startErr = socketRs.getError()
//                this.cancel()
//            }
//        }
//
//
////        val timeOutRs = Sleeper.delayUntil2(
////            timeOut = startTimeOut,
////            timeOutMessage = "Time out when trying to start heart beat service",
////            failSignal = { true },
////        ) { startTimeOutJob ->
////        }
//
//        if (startErr != null) {
//            return Err(startErr as ErrorReport)
//        }
//        val rt = startTimeOutJob.await()
////        val rt = timeOutRs
//        if (rt is Err) {
//            job?.cancel()
//            return Err(rt.error)
//        }
//        return rt
//    }

//    /**
//     * wait until this service goes live
//     */
//    private suspend fun waitTillLive(): Result<Unit, ErrorReport> {
//        val waitRs2 = Sleeper.delayUntil(50, startTimeOut) { this.isHBAlive() }
//        val rt2 = waitRs2.mapError {
//            ErrorReport(
//                header = HBServiceErrors.CantStartHBService.header,
//                data = HBServiceErrors.CantStartHBService.Data("Time out when waiting for HB to come live"),
//            )
//        }
//        return rt2
//    }

    /**
     * Justification for throwing an exception here: the programmer must make sure this function must not be called if the service is not running.
     */
    override fun isHBAlive(): Boolean {
        if (this.isServiceRunning()) {
            val rt = this.currentLives > 0
            return rt
        } else {
            throw HBServiceCrashException()
        }
    }

    /**
     * wait until hb channel in the python side actually goes live
     */
    suspend fun waitHBALive(timeOut:Long = this.startTimeOut): Result<Unit, ErrorReport> {
        try {
            val o = withTimeout(timeOut) {
                hbLiveWaitJob.await()
            }
            return o
        } catch (e: Throwable) {
            return CommonErrors.TimeOut.report("timeOutMessage").toErr()
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
            job?.cancelAndJoin()
        }
        return Ok(Unit)
    }

    override fun stop(): Result<Unit, ErrorReport> {
        if (this.isServiceRunning()) {
            job?.cancel()
        }
        return Ok(Unit)
    }
}
