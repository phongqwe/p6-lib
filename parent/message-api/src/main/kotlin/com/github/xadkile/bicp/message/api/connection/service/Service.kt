package com.github.xadkile.bicp.message.api.connection.service

interface Service {
    fun start()
    fun stop()
    fun isRunning(): Boolean
//    fun state():ServiceState
}

enum class ServiceState {
    UNDECIDED,INIT, RUNNING, STOPPED
}
