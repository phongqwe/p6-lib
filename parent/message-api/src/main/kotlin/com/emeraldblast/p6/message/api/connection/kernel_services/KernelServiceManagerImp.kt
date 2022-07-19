package com.emeraldblast.p6.message.api.connection.kernel_services

import com.emeraldblast.p6.common.exception.error.CommonErrors
import com.emeraldblast.p6.common.exception.error.ErrorReport
import com.emeraldblast.p6.message.api.connection.kernel_context.KernelContext
import com.emeraldblast.p6.message.api.connection.kernel_context.KernelTimeOut
import com.emeraldblast.p6.message.api.connection.service.Service
import com.emeraldblast.p6.message.api.connection.service.errors.ServiceErrors
import com.emeraldblast.p6.message.api.connection.service.heart_beat.HeartBeatService
import com.emeraldblast.p6.message.api.connection.service.heart_beat.HeartBeatServiceFactory
import com.emeraldblast.p6.message.api.connection.service.iopub.IOPubListenerService
import com.emeraldblast.p6.message.api.connection.service.iopub.IOPubListenerServiceFactory
import com.emeraldblast.p6.message.api.connection.service.zmq_services.ZMQListenerService
import com.emeraldblast.p6.message.api.connection.service.zmq_services.imp.SyncREPServiceFactory
import com.emeraldblast.p6.message.api.connection.service.zmq_services.msg.P6Response
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import javax.inject.Inject

class KernelServiceManagerImp @Inject constructor(
    private val kernel: KernelContext,
    private val heartBeatServiceFactory: HeartBeatServiceFactory,
    private val ioPubListenerServiceFactory: IOPubListenerServiceFactory,
    private val syncRepServiceFactory: SyncREPServiceFactory,
) : KernelServiceManager {

    private var _hbService: HeartBeatService = heartBeatServiceFactory.create(
        kernelContext = kernel,
        liveCount = 20,
        pollTimeOut = 1000,
        startTimeOut = kernel.kernelConfig?.timeOut?.serviceInitTimeOut ?: KernelTimeOut.defaultTimeOut
    )
    private var _ioPubService: IOPubListenerService = ioPubListenerServiceFactory.create(
        kernelContext = kernel,
        defaultHandler = {},
        parseExceptionHandler = {},
        startTimeOut = kernel.kernelConfig?.timeOut?.serviceInitTimeOut ?: KernelTimeOut.defaultTimeOut
    )
    private var _zmqREPService: ZMQListenerService<P6Response> = syncRepServiceFactory.create(
        kernelContext = kernel,
    )

    override val hbService: HeartBeatService
        get() = _hbService
    override val ioPubService: IOPubListenerService
        get() = _ioPubService
    override val zmqREPService: ZMQListenerService<P6Response>
        get() = _zmqREPService
    override val status: ServiceManagerStatus
        get() = ServiceManagerStatus(
            HBServiceRunning = this.hbService.isRunning(),
            ioPubListenerServiceRunning = this.ioPubService.isRunning(),
            zmqRepServiceRunning = this.zmqREPService.isRunning(),
        )

    override suspend fun startAll(): Result<Unit, ErrorReport> {
        if (kernel.isKernelRunning()) {
            val hbStartRs: Result<Unit, ErrorReport> = this._hbService.start()
            if (hbStartRs is Err) {
                this.hbService.stop()
                return hbStartRs
            }

            val ioPubStartRs: Result<Unit, ErrorReport> = this._ioPubService.start()
            if (ioPubStartRs is Err) {
                this.ioPubService.stop()
                return ioPubStartRs
            }

            val zmqListenerServiceStartRs = this._zmqREPService.start()
            if (zmqListenerServiceStartRs is Err) {
                this.zmqREPService.stop()
                return zmqListenerServiceStartRs
            }
            return Ok(Unit)
        } else {
            return KernelServiceManagerErrors.CantStartServices.report("Can't start kernel services because kernel is down")
                .toErr()
        }
    }

    override fun areServicesRunning(): Boolean {
        return status.areAllRunning()
    }

    override fun stopAll(): Result<Unit, ErrorReport> {

        val errorList = mutableListOf<ErrorReport>()

        val ioPubStopRs = this.ioPubService.stop()
        if (ioPubStopRs is Err) {
            errorList.add(ioPubStopRs.error)
        }

        val hbStopRs = this.hbService.stop()
        if (hbStopRs is Err) {
            errorList.add(hbStopRs.error)
        }

        val zmqRepStopRs = this.zmqREPService.stop()
        if (zmqRepStopRs is Err) {
            errorList.add(zmqRepStopRs.error)
        }

        if (errorList.isNotEmpty()) {
            return Err(
                ErrorReport(
                    header = CommonErrors.MultipleErrors.header,
                    data = CommonErrors.MultipleErrors.Data(errorList)
                )
            )
        } else {
            return Ok(Unit)
        }
    }

    override fun getIOPubListenerServiceRs(): Result<IOPubListenerService, ErrorReport> {
        return Ok(this.ioPubService)
    }

    override fun getHeartBeatServiceRs(): Result<HeartBeatService, ErrorReport> {
        return Ok(this.hbService)
    }

    override fun getZmqREPServiceRs(): Result<ZMQListenerService<P6Response>, ErrorReport> {
        return Ok(this.zmqREPService)
    }

//    private fun <T> getService(service: Service?, serviceName: String): Result<T, ErrorReport> {
//        if (service != null) {
//            return Ok(service as T)
//        } else {
//            return ServiceErrors.ServiceNull.report("${serviceName} is null").toErr()
//        }
//    }

}
