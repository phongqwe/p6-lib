package com.github.xadkile.bicp.message.api.connection.service

interface Service :ServiceReadOnly{
    fun start()
    fun stop()

}

interface ServiceReadOnly {
    fun isRunning(): Boolean
    fun isNotRunning():Boolean{
        return !this.isRunning()
    }
}
