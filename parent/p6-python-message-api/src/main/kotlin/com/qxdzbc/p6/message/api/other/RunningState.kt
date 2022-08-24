package com.qxdzbc.p6.message.api.other

import com.qxdzbc.common.error.ErrorReport

interface RunningState {
    fun isRunning(): Boolean
    fun isRunningRs(): com.github.michaelbull.result.Result<Unit, ErrorReport>
    fun isNotRunning():Boolean{
        return !this.isRunning()
    }
}
