package com.github.xadkile.p6.message.api.connection.service.heart_beat

import com.github.michaelbull.result.*
import com.github.xadkile.p6.exception.lib.error.CommonErrors
import com.github.xadkile.p6.exception.lib.error.ErrorReport
import com.github.xadkile.p6.message.api.connection.kernel_context.context_object.SocketProvider
import com.github.xadkile.p6.message.api.connection.service.heart_beat.errors.HBServiceErrors
import com.github.xadkile.p6.message.api.other.Sleeper
import kotlinx.coroutines.*
import org.zeromq.ZContext
import org.zeromq.ZMQ

/**
 * Must not invoke the constructor directly unless in testing. An instance of this is provided by [IPythonContext].
 * This service is non-recoverable. If it detects a dead signal, it will remain dead, even when the service is back.
 * Due to its non-recoverable nature, it should only be bound and used along an IPython context that control its (the hb service)'s life cycle from creation, to stop.
 *
 * This implementation is exactly like LiveCountHeartBeatServiceThread, but run on coroutine instead of thread
 */
internal class LiveCountHeartBeatServiceCoroutine constructor(
    zContext: ZContext,
    private val socketProvider: SocketProvider,
    liveCount: Int = 100,
    pollTimeout: Long = 1000,
    val startTimeOut: Long = 50_000,
    cScope: CoroutineScope,
    cDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AbstractLiveCountHeartBeatServiceCoroutine(zContext, liveCount, pollTimeout, cScope, cDispatcher) {


    /**
     * init resources and start service thread
     */
    override suspend fun start(): Result<Unit, ErrorReport> {
        if (this.isServiceRunning()) {
            return Ok(Unit)
        }
        this.job = cScope.launch(cDispatcher) {
            val socket = socketProvider.heartBeatSocket()
            socket.use { sk ->
                val poller = zContext.createPoller(1)
                poller.register(sk, ZMQ.Poller.POLLIN)
                poller.use {
                    while (isActive) {
                        val checkRs = check(poller, sk)
                        val isAlive: Boolean = checkRs is Ok
                        if (isAlive) {
                            currentLives = liveCount
                        } else {
                            // rmd: only reduce life if there are lives left to prevent underflow of int
                            if (currentLives > 0) {
                                currentLives -= 1
                            }
                        }
                    }
                }
            }
        }
        val rt = this.waitTillLive()
        if (rt is Err) {
            bluntStop()
            val report = ErrorReport(
                header = CommonErrors.TimeOut,
                data = CommonErrors.TimeOut.Data("Time out when trying to start IOPub service")
            )
            return Err(report)
        }
        return rt
    }

    private suspend fun waitTillLive(): Result<Unit, ErrorReport> {
        val waitRs = Sleeper.delayUntil(50, startTimeOut) { this.isRunning() }
        val loc = "${this.javaClass.canonicalName}:waitTillLive"
        val rt = waitRs.mapError {
            ErrorReport(
                header = CommonErrors.TimeOut,
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
                header = HBServiceErrors.CantStartHBService,
                data = HBServiceErrors.CantStartHBService.Data("Time out when waiting for HB to come live"),
                loc = loc
            )
        }
        return rt2
    }

}
