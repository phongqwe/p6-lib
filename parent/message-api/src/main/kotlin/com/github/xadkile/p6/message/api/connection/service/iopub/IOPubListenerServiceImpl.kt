package com.github.xadkile.p6.message.api.connection.service.iopub

import com.github.michaelbull.result.*
import com.github.xadkile.p6.common.exception.lib.error.ErrorReport
import com.github.xadkile.p6.message.api.connection.kernel_context.KernelContextReadOnly
import com.github.xadkile.p6.message.api.connection.kernel_context.errors.KernelErrors
import com.github.xadkile.p6.message.api.connection.service.iopub.errors.IOPubServiceErrors
import com.github.xadkile.p6.message.api.message.protocol.JPRawMessage
import com.github.xadkile.p6.message.api.message.protocol.MsgType
import com.github.xadkile.p6.message.api.message.protocol.data_interface_definition.IOPub
import com.github.xadkile.p6.message.api.other.Sleeper
import kotlinx.coroutines.*
import org.zeromq.ZMQ
import org.zeromq.ZMsg

/**
 * Listen to pub msg from IOPub channel and dispatch msg to appropriate handlers.
 * [defaultHandler] to handle msg type that don't have a specific handler.
 * [parseExceptionHandler] to handle exception of unable to parse zmq message.
 */
class IOPubListenerServiceImpl internal constructor(
    private val kernelContext: KernelContextReadOnly,
    private val defaultHandler: ((msg: JPRawMessage) -> Unit)? = { /*do nothing*/ },
    private val parseExceptionHandler: suspend (exception: ErrorReport) -> Unit= { /*do nothing*/ },
    private val handlerContainer: MsgHandlerContainer = HandlerContainerImp(),
    private val externalScope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val startTimeOut:Long = 50_000
) : IOPubListenerService {

    private var job: Job? = null

    /**
     * this will start this listener on a coroutine that runs concurrently.
     */
    override suspend fun start(): Result<Unit, ErrorReport> {
        if (this.isRunning()) {
            return Ok(Unit)
        }

        if(kernelContext.isKernelRunning().not()){
            val report = ErrorReport(
                type = KernelErrors.KernelDown,
                data = KernelErrors.KernelDown.Data("${this.javaClass.canonicalName}:start")
            )
            return Err(report)
        }

        val socket: ZMQ.Socket = kernelContext.getSocketProvider().unwrap().ioPubSocket()
        // x: add default handler
        if(defaultHandler!=null){
            this.addDefaultHandler(MsgHandlers.withUUID(MsgType.DEFAULT, defaultHandler))
        }
        job = externalScope.launch(dispatcher) {
            socket.use {
                // x: start the service loop
                // x: when the kernel is down, this service simply does not do anything. Just hang there.
                while (isActive) {
                    // x: this listener is passive, so it can start listening when the kernel is up, no need to wait for heartbeat service
                    if (kernelContext.isKernelRunning()) {
                        val msg = ZMsg.recvMsg(it, ZMQ.DONTWAIT)
                        if (msg != null) {
                            val parseResult = JPRawMessage.fromPayload(msg.map { f -> f.data })
                            when (parseResult) {
                                is Ok -> {
                                    val rawMsg: JPRawMessage = parseResult.value
                                    val msgType: MsgType = extractMsgType(rawMsg.identities)
                                    dispatchMsgToHandlers(msgType, rawMsg)
                                }
                                else -> {
                                    parseExceptionHandler(parseResult.unwrapError())
                                }
                            }
                        }
                    }
                }
            }
        }
        val waitRs = Sleeper.delayUntil(10, startTimeOut) { this.isRunning() }
        if(waitRs is Err){
            this.bluntStop()
            val report = ErrorReport(
                type= IOPubServiceErrors.CantStartIOPubServiceTimeOut,
                data = IOPubServiceErrors.CantStartIOPubServiceTimeOut.Data("Time out when trying to start IOPub service"),
                loc = "${this.javaClass.canonicalName}:start"
            )
            return Err(report)
        }else{
            return Ok(Unit)
        }
    }

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

    override suspend fun stop(): Result<Unit, ErrorReport> {
        if (this.isRunning()) {
            bluntStop()
        }
        return Ok(Unit)
    }

    private suspend fun bluntStop(){
        job?.cancelAndJoin()
        this.job = null
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
