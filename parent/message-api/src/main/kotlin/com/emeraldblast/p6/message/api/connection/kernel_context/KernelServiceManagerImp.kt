package com.emeraldblast.p6.message.api.connection.kernel_context

import com.emeraldblast.p6.common.exception.error.CommonErrors
import com.emeraldblast.p6.common.exception.error.ErrorReport
import com.emeraldblast.p6.message.api.connection.kernel_context.errors.KernelErrors
import com.emeraldblast.p6.message.api.connection.service.Service
import com.emeraldblast.p6.message.api.connection.service.errors.ServiceErrors
import com.emeraldblast.p6.message.api.connection.service.heart_beat.HeartBeatService
import com.emeraldblast.p6.message.api.connection.service.heart_beat.HeartBeatServiceFactory
import com.emeraldblast.p6.message.api.connection.service.iopub.IOPubListenerService
import com.emeraldblast.p6.message.api.connection.service.iopub.IOPubListenerServiceFactory
import com.emeraldblast.p6.message.api.connection.service.iopub.errors.IOPubServiceErrors
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

    private var _hbService: HeartBeatService? = null
    private var _ioPubService: IOPubListenerService? = null
    private var _zmqREPService: ZMQListenerService<P6Response>? = null

    override val hbService: HeartBeatService?
        get() = _hbService
    override val ioPubService: IOPubListenerService?
        get() = _ioPubService
    override val zmqREPService: ZMQListenerService<P6Response>?
        get() = _zmqREPService

    override suspend fun startAll(): Result<Unit, ErrorReport> {
        if (kernel.isKernelRunning()) {
            val hbSv = heartBeatServiceFactory.create(
                kernelContext = kernel,
                liveCount = 20,
                pollTimeOut = 1000,
                startTimeOut = kernel.kernelConfig?.timeOut?.serviceInitTimeOut ?: KernelTimeOut.defaultTimeOut
            )
            this._hbService = hbSv

            val hbStartRs: Result<Unit, ErrorReport> = hbSv.start()
            if (hbStartRs is Err) {
                this.hbService?.stop()
                this._hbService = null
                return hbStartRs
            }

            val ioPubSv = ioPubListenerServiceFactory.create(
                kernelContext = kernel,
                defaultHandler = {},
                parseExceptionHandler = {},
                startTimeOut = kernel.kernelConfig?.timeOut?.serviceInitTimeOut ?: KernelTimeOut.defaultTimeOut
            )
            this._ioPubService = ioPubSv

            val ioPubStartRs: Result<Unit, ErrorReport> = ioPubSv.start()
            if (ioPubStartRs is Err) {
                this.ioPubService?.stop()
                this._ioPubService = null
                return ioPubStartRs
            }

            val zmqREPService = syncRepServiceFactory.create(
                kernelContext = kernel,
            )

            this._zmqREPService = zmqREPService
            val zmqListenerServiceStartRs = zmqREPService.start()
            if (zmqListenerServiceStartRs is Err) {
                this.zmqREPService?.stop()
                this._zmqREPService = null
                return zmqListenerServiceStartRs
            }

            return Ok(Unit)

        } else {
            return KernelServiceManagerErrors.CantStartServices
                .report("Can't start kernel services because kernel is down")
                .toErr()
        }
    }

    override fun areServicesRunning(): Boolean {
        val hbRunning = this.hbService?.isServiceRunning() ?: false
        val ioPubRunning = this.ioPubService?.isRunning() ?: false
        return hbRunning && ioPubRunning
    }

    override fun stopAll(): Result<Unit, ErrorReport> {

        val errorList = mutableListOf<ErrorReport>()
        val ioPubStopRs = this.ioPubService?.stop() ?: Ok(Unit)
        if (ioPubStopRs is Err) {
            errorList.add(ioPubStopRs.error)
        } else {
            this._ioPubService = null
        }

        val hbStopRs = this.hbService?.stop() ?: Ok(Unit)
        if (hbStopRs is Err) {
            errorList.add(hbStopRs.error)
        } else {
            this._hbService = null
        }

        val zmqRepStopRs = this.zmqREPService?.stop() ?: Ok(Unit)
        if (zmqRepStopRs is Err) {
            errorList.add(zmqRepStopRs.error)
        } else {
            this._zmqREPService = null
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
        val sv = getService<IOPubListenerService>(this.ioPubService, "IO Pub service")
        return sv
    }


    override fun getHeartBeatServiceRs(): Result<HeartBeatService, ErrorReport> {
        val sv = getService<HeartBeatService>(this.hbService, "heart beat service")
        return sv
    }

    override fun getZmqREPServiceRs(): Result<ZMQListenerService<P6Response>, ErrorReport> {
        val sv = getService<ZMQListenerService<P6Response>>(this.zmqREPService, "zmq rep service")
        return sv
    }

    private fun <T> getService(service: Service?, serviceName: String): Result<T, ErrorReport> {
        if (service != null) {
            return Ok(service as T)
        } else {
            return ServiceErrors.ServiceNull.report("${serviceName} is null").toErr()
        }
    }

}
