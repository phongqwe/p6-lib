package com.github.xadkile.bicp.message.api.other

interface RunningState {
    fun isRunning(): Boolean
    fun isNotRunning():Boolean{
        return !this.isRunning()
    }
}
