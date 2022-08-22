package com.qxdzbc.p6.message.api.connection.service.iopub

import com.qxdzbc.p6.common.exception.error.ErrorReport
import com.qxdzbc.p6.message.api.connection.kernel_context.KernelContextReadOnly
import com.qxdzbc.p6.message.api.connection.kernel_context.KernelCoroutineScope
import com.qxdzbc.p6.message.api.connection.kernel_context.KernelTimeOut
import com.qxdzbc.p6.message.api.connection.kernel_context.errors.KernelErrors
import com.qxdzbc.p6.message.api.connection.service.iopub.errors.IOPubServiceErrors
import com.qxdzbc.p6.message.api.connection.service.iopub.handler.MsgHandler
import com.qxdzbc.p6.message.api.connection.service.iopub.handler.MsgHandlers
import com.qxdzbc.p6.message.api.connection.service.iopub.handler.execution_handler.*
import com.qxdzbc.p6.message.api.message.protocol.JPRawMessage
import com.qxdzbc.p6.message.api.message.protocol.MsgType
import com.qxdzbc.p6.message.api.message.protocol.data_interface_definition.IOPub
import com.qxdzbc.p6.message.di.ServiceCoroutineDispatcher
import com.github.michaelbull.result.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import org.zeromq.ZMQ
import org.zeromq.ZMsg

/**
 * Listen to pub msg from IOPub channel and dispatch msg to appropriate handlers.
 * [defaultHandler] to handle msg type that don't have a specific handler.
 * [parseExceptionHandler] to handle exception of unable to parse zmq message.
 */
class IOPubListenerServiceImp @AssistedInject constructor(
    @Assisted private val kernelContext: KernelContextReadOnly,
    @Assisted private val defaultHandler: ((msg: JPRawMessage) -> Unit)? = { /*do nothing*/ },
    @Assisted private val parseExceptionHandler: (exception: ErrorReport) -> Unit = { /*do nothing*/ },
    @Assisted private val startTimeOut: Long = KernelTimeOut.defaultTimeOut,
    private val handlerContainer: MsgHandlerContainer = MsgHandlerContainerImp(),
    @KernelCoroutineScope
    private val externalScope: CoroutineScope,
    @ServiceCoroutineDispatcher
    private val dispatcher: CoroutineDispatcher,
    override val executeResultHandler: ExecuteResultHandler = ExecuteResultHandlerImp(),
    override val executeErrorHandler: ExecuteErrorHandler = ExecuteErrorHandlerImp(),
    override val idleExecutionStatusHandler: IdleExecutionStatusHandler = ExecutionStatusHandlerImp.Idle(),
    override val busyExecutionStatusHandler: BusyExecutionStatusHandler = ExecutionStatusHandlerImp.Busy(),
    override val displayDataHandler: DisplayDataHandler = DisplayDataHandlerImp()
) : IOPubListenerService {

    init {
        this.addHandlers(
            listOf(
                this.executeResultHandler,
                this.executeErrorHandler,
                this.idleExecutionStatusHandler,
                this.busyExecutionStatusHandler,
                this.displayDataHandler,
            )
        )
    }

    private var job: Job? = null

    /**
     * this will start this listener on a coroutine that runs concurrently.
     */
    override suspend fun start(): Result<Unit, ErrorReport> {
        if (this.isRunning()) {
            return Ok(Unit)
        }

        if (kernelContext.isKernelRunning().not()) {
            val report = KernelErrors.KernelDown.report("Can't start IOPub listener service because the kernel is down")
            return Err(report)
        }

        // x: add default handler
        if (defaultHandler != null) {
            this.addDefaultHandler(MsgHandlers.withUUID(MsgType.DEFAULT, defaultHandler))
        }

        try {
            withTimeout(startTimeOut) {
                job = externalScope.launch(dispatcher) {
                    val socket: ZMQ.Socket = kernelContext.getSocketFactory().unwrap().ioPubSocket()
                    socket.use {
                        // x: start the service loop
                        while (isActive) {
                            if (kernelContext.isKernelRunning()) {
                                val msg = ZMsg.recvMsg(it)
                                if (msg != null) {
                                    val jpRawMsgRs = JPRawMessage.fromPayload(msg.map { f -> f.data })
                                    when (jpRawMsgRs) {
                                        is Ok -> {
                                            val rawMsg: JPRawMessage = jpRawMsgRs.value
                                            val msgType: MsgType = extractMsgType(rawMsg.identities)
                                            dispatchMsgToHandlers(msgType, rawMsg)
                                        }
                                        else -> {
                                            parseExceptionHandler(jpRawMsgRs.unwrapError())
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            this.job?.cancel()
            val report = ErrorReport(
                header = IOPubServiceErrors.CantStartIOPubServiceTimeOut.header,
                data = IOPubServiceErrors.CantStartIOPubServiceTimeOut.Data("Time out when trying to start IOPub service"),
            )
            return Err(report)
        }

        return Ok(Unit)
    }
//    /**
//     * this will start this listener on a coroutine that runs concurrently.
//     */
//    override suspend fun start(): Result<Unit, ErrorReport> {
//        if (this.isRunning()) {
//            return Ok(Unit)
//        }
//
//        if (kernelContext.isKernelRunning().not()) {
//            val report = KernelErrors.KernelDown.report("Can't start IOPub listener service because the kernel is down")
//            return Err(report)
//        }
//
//        // x: add default handler
//        if (defaultHandler != null) {
//            this.addDefaultHandler(MsgHandlers.withUUID(MsgType.DEFAULT, defaultHandler))
//        }
//        this.job = externalScope.launch(dispatcher) {
//            val socket: ZMQ.Socket = kernelContext.getSocketProvider().unwrap().ioPubSocket()
//            socket.use {
//                // x: start the service loop
//                // x: when the kernel is down, this service simply does not do anything. Just hang there.
//                while (isActive) {
//                    // x: this listener is passive, so it can start listening when the kernel is up, no need to wait for heartbeat service
//                    if (kernelContext.isKernelRunning()) {
//                        val msg = ZMsg.recvMsg(it)
//                        if (msg != null) {
//                            val parseResult = JPRawMessage.fromPayload(msg.map { f -> f.data })
//                            when (parseResult) {
//                                is Ok -> {
//                                    val rawMsg: JPRawMessage = parseResult.value
//                                    val msgType: MsgType = extractMsgType(rawMsg.identities)
//                                    dispatchMsgToHandlers(msgType, rawMsg)
//                                }
//                                else -> {
//                                    parseExceptionHandler(parseResult.unwrapError())
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        val waitRs = Sleeper.delayUntil(10, startTimeOut) { this.isRunning() }
//        if (waitRs is Err) {
//            this.job?.cancel()
//            val report = ErrorReport(
//                header = IOPubServiceErrors.CantStartIOPubServiceTimeOut.header,
//                data = IOPubServiceErrors.CantStartIOPubServiceTimeOut.Data("Time out when trying to start IOPub service"),
//            )
//            return Err(report)
//        } else {
//            return Ok(Unit)
//        }
//    }

    /**
     * [msgIdentity] always ends with msg type
     */
    private fun extractMsgType(msgIdentity: String): MsgType {
        val msgType: MsgType = when {

            msgIdentity.endsWith(IOPub.ExecuteResult.msgType.text) -> IOPub.ExecuteResult.msgType

            msgIdentity.endsWith(IOPub.Status.msgType.text) -> IOPub.Status.msgType

            msgIdentity.endsWith(IOPub.ExecuteError.msgType.text) -> IOPub.ExecuteError.msgType

            // TODO add more msg type here

            else -> {
                MsgType.DEFAULT
            }
        }
        return msgType
    }

    override suspend fun stopJoin(): Result<Unit, ErrorReport> {
        if (this.isRunning()) {
            job?.cancelAndJoin()
        }
        return Ok(Unit)
    }

    override fun stop(): Result<Unit, ErrorReport> {
        if (this.isRunning()) {
            this.job?.cancel()
        }
        return Ok(Unit)
    }

    override fun isRunning(): Boolean {
        return this.job?.isActive == true
    }

    /**
     * dispatch a parsed message to handlers
     */
    private fun dispatchMsgToHandlers(
        msgType: MsgType,
        msg: JPRawMessage,
    ) {
        getHandlers(msgType).forEach {
            it.handle(msg)
        }
    }

    override fun addHandler(handler: MsgHandler) {
        this.handlerContainer.addHandler(handler)
    }

    override fun getHandlers(msgType: MsgType): List<MsgHandler> {
        return this.handlerContainer.getHandlers(msgType)
    }

    override fun containHandler(id: String): Boolean {
        return this.handlerContainer.containHandler(id)
    }

    override fun containHandler(handler: MsgHandler): Boolean {
        return this.handlerContainer.containHandler(handler)
    }

    override fun removeHandler(handlerId: String) {
        this.handlerContainer.removeHandler(handlerId)
    }

    override fun removeHandler(handler: MsgHandler) {
        this.handlerContainer.removeHandler(handler)
    }

    override fun allHandlers(): List<MsgHandler> {
        return this.handlerContainer.allHandlers()
    }

    override fun isEmpty(): Boolean {
        return this.handlerContainer.isEmpty()
    }

    override fun isNotEmpty(): Boolean {
        return this.handlerContainer.isNotEmpty()
    }
}
