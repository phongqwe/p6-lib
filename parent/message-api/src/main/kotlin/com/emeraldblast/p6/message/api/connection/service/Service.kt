package com.emeraldblast.p6.message.api.connection.service

import com.github.michaelbull.result.Result
import com.emeraldblast.p6.common.exception.error.ErrorReport
import com.emeraldblast.p6.message.api.other.RunningState

interface Service : RunningState{
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
