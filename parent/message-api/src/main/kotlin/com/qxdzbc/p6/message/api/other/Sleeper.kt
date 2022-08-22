package com.qxdzbc.p6.message.api.other

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.qxdzbc.p6.common.exception.error.CommonErrors
import com.qxdzbc.p6.common.exception.error.ErrorReport
import kotlinx.coroutines.*

object Sleeper {

    suspend fun delayUntil(waitPeriod: Long, predicate: () -> Boolean) {
        while (predicate() == false) {
            delay(waitPeriod)
        }
    }
    fun waitBlockUntil(waitPeriod: Long, timeOut: Long, predicate: () -> Boolean): Result<Unit, ErrorReport> {
        var time = 0L
        while (predicate() == false && time < timeOut) {
            Thread.sleep(waitPeriod)
            time += waitPeriod
        }
        if (time < timeOut) {
            return Ok(Unit)
        } else {
            return Err(
                ErrorReport(
                    header = CommonErrors.TimeOut.header,
                    data = CommonErrors.TimeOut.Data("timeout in Sleeper.delay()")
                )
            )
        }
    }
    suspend fun delayUntil(waitPeriod: Long, timeOut: Long, predicate: () -> Boolean): Result<Unit, ErrorReport> {
        var time = 0L
        while (predicate() == false && time < timeOut) {
            delay(waitPeriod)
            time += waitPeriod
        }
        if (time < timeOut) {
            return Ok(Unit)
        } else {
            return Err(
                ErrorReport(
                    header = CommonErrors.TimeOut.header,
                    data = CommonErrors.TimeOut.Data("timeout in Sleeper.delay()")
                )
            )
        }
    }

    suspend fun delayUntil2(
        timeOut: Long,
        failSignal: () -> Boolean,
        timeOutMessage: String = "",
        work:(timeOutJob:CompletableDeferred<Result<Unit, ErrorReport>>)->Unit,
    ): Result<Unit, ErrorReport> {
        val liveSignal:CompletableDeferred<Result<Unit, ErrorReport>> = CompletableDeferred()
        coroutineScope {
            launch(Dispatchers.IO) {
                delay(timeOut)
                if (failSignal()) {
                    if(liveSignal.isActive){
                        liveSignal.complete(CommonErrors.TimeOut.report(timeOutMessage).toErr())
                    }
                }
            }
            launch(Dispatchers.IO) {
                work(liveSignal)
                if(liveSignal.isActive){
                    liveSignal.complete(Ok(Unit))
                }
            }
        }
        return liveSignal.await()
    }
}


