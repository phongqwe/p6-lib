package com.emeraldblast.p6.message.api.other

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.emeraldblast.p6.common.exception.error.CommonErrors
import com.emeraldblast.p6.common.exception.error.ErrorReport
import kotlinx.coroutines.delay

object Sleeper {

    suspend fun delayUntil(waitTime:Long, predicate: () -> Boolean){
        while(predicate()==false){
            delay(waitTime)
        }
    }

    suspend fun delayUntil(waitTime:Long, timeOut:Long, predicate: () -> Boolean):Result<Unit, ErrorReport>{
        var time = 0L
        while(predicate()==false && time < timeOut){
            delay(waitTime)
            time += waitTime
        }
        if(time<timeOut){
            return Ok(Unit)
        }else{
            return Err(
                ErrorReport(
                header =  CommonErrors.TimeOut.header,
                data = CommonErrors.TimeOut.Data("timeout in Sleeper.delay()")
            )
            )
        }
    }
}


