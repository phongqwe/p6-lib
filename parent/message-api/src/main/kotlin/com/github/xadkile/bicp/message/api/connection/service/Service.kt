package com.github.xadkile.bicp.message.api.connection.service

import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.other.RunningState

interface Service :ServiceReadOnly{
    fun start()
    fun stop()

}

interface ServiceReadOnly : RunningState

