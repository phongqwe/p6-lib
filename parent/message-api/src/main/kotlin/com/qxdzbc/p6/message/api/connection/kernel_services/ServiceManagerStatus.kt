package com.qxdzbc.p6.message.api.connection.kernel_services

data class ServiceManagerStatus(
    val HBServiceRunning:Boolean,
    val ioPubListenerServiceRunning:Boolean,
    val zmqRepServiceRunning:Boolean
) {
    fun areAllRunning():Boolean{
        return listOf(HBServiceRunning, ioPubListenerServiceRunning,zmqRepServiceRunning).all{it}
    }
}
