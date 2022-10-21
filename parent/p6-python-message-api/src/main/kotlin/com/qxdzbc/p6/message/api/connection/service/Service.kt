package com.qxdzbc.p6.message.api.connection.service

import com.github.michaelbull.result.Result
import com.qxdzbc.common.error.ErrorReport
import com.qxdzbc.p6.message.api.connection.service.errors.ServiceErrors
import com.qxdzbc.p6.message.api.connection.service.iopub.errors.IOPubServiceErrors
import com.qxdzbc.p6.message.api.other.RunningState
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok

interface Service : RunningState{
    override fun isRunningRs(): Result<Unit, ErrorReport> {
        if(this.isRunning()){
            return Ok(Unit)
        }else{
            return ServiceErrors.ServiceNotRunning.report("service ${this::class.simpleName} is not running").toErr()
        }
    }

    /**
     * Start a service. After this function returns, it is guaranteed that this instance of service is fully functional.
     * Calling start() on an already started service doesn't do anything.
     */
    suspend fun start(): Result<Unit, ErrorReport>

    /**
     * join then stop. This will wait for the last iteration to complete
     */
    suspend fun stopJoin(): Result<Unit, ErrorReport>
    /**
     * Stop this service. This method guarantees that this service is completely stopped after this method returns.
     * Calling stop on an already stopped service doesn't do anything.
     */
    fun stop(): Result<Unit, ErrorReport>
}
