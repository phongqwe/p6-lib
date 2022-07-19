package com.emeraldblast.p6.message.api.connection.kernel_services

import com.emeraldblast.p6.common.exception.error.ErrorReport
import com.emeraldblast.p6.message.api.connection.service.heart_beat.HeartBeatService
import com.emeraldblast.p6.message.api.connection.service.iopub.IOPubListenerService
import com.emeraldblast.p6.message.api.connection.service.zmq_services.ZMQListenerService
import com.emeraldblast.p6.message.api.connection.service.zmq_services.msg.P6Response
import com.github.michaelbull.result.Result

interface KernelServiceManager {
    val hbService: HeartBeatService?
    val ioPubService: IOPubListenerService?
    val zmqREPService: ZMQListenerService<P6Response>?

    val status:ServiceManagerStatus

    fun getHeartBeatServiceRs():Result<HeartBeatService, ErrorReport>
    fun getZmqREPServiceRs():Result<ZMQListenerService<P6Response>,ErrorReport>
    fun getIOPubListenerServiceRs(): Result<IOPubListenerService, ErrorReport>

    suspend fun startAll(): Result<Unit, ErrorReport>
    fun stopAll(): Result<Unit, ErrorReport>
    fun areServicesRunning(): Boolean
}
