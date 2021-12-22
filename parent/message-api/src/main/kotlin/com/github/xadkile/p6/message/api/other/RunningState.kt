package com.github.xadkile.p6.message.api.other

interface RunningState {
    fun isRunning(): Boolean
    fun isNotRunning():Boolean{
        return !this.isRunning()
    }
}
