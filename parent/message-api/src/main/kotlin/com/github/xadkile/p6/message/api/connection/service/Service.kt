package com.github.xadkile.p6.message.api.connection.service

import com.github.michaelbull.result.Result
import com.github.xadkile.p6.common.exception.error.ErrorReport
import com.github.xadkile.p6.message.api.other.RunningState

interface Service : RunningState{
    /**
     * Start a service. After this function returns, it is guaranteed that this instance of service is fully functional.
     * Calling start() on an already started listener doesn't do anything.
     */
    suspend fun start(): Result<Unit, ErrorReport>

    /**
     * Stop this service. This method guarantees that this service is completely stopped after this method returns.
     * Calling stop on an already stop service doesn't do anything.
     */
    suspend fun stop(): Result<Unit, ErrorReport>
}
