package com.qxdzbc.p6.message.api.connection.kernel_services

import com.qxdzbc.common.error.ErrorReport
import com.qxdzbc.p6.message.api.connection.service.heart_beat.HeartBeatService
import com.qxdzbc.p6.message.api.connection.service.iopub.IOPubListenerService
import com.qxdzbc.p6.message.api.connection.service.zmq_services.ZMQListenerService
import com.qxdzbc.p6.message.api.connection.service.zmq_services.msg.P6Response
import com.github.michaelbull.result.Result

interface KernelServiceManager {
    val hbService: HeartBeatService
    val ioPubService: IOPubListenerService
    val zmqREPService: ZMQListenerService<P6Response>

    val status:ServiceManagerStatus

    fun getHeartBeatServiceRs():Result<HeartBeatService, ErrorReport>
    fun getZmqREPServiceRs():Result<ZMQListenerService<P6Response>, ErrorReport>
    fun getIOPubListenerServiceRs(): Result<IOPubListenerService, ErrorReport>

    suspend fun startAll(): Result<Unit, ErrorReport>
    fun stopAll(): Result<Unit, ErrorReport>
    fun areServicesRunning(): Boolean
}
