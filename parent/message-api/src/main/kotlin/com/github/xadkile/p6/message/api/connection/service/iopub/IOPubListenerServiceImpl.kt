package com.github.xadkile.p6.message.api.connection.service.iopub

import com.github.michaelbull.result.*
import com.github.xadkile.p6.exception.ExceptionInfo
import com.github.xadkile.p6.message.api.connection.kernel_context.KernelContext
import com.github.xadkile.p6.message.api.connection.kernel_context.KernelContextReadOnlyConv
import com.github.xadkile.p6.message.api.connection.kernel_context.exception.KernelIsDownException
import com.github.xadkile.p6.message.api.connection.service.heart_beat.exception.HBIsDeadException
import com.github.xadkile.p6.message.api.connection.service.iopub.exception.CantStartIOPubServiceException
import com.github.xadkile.p6.message.api.msg.protocol.JPRawMessage
import com.github.xadkile.p6.message.api.msg.protocol.MsgType
import com.github.xadkile.p6.message.api.msg.protocol.data_interface_definition.IOPub
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
    private val kernelContext: KernelContextReadOnlyConv,
    private val defaultHandler: (msg: JPRawMessage) -> Unit,
    private val parseExceptionHandler: suspend (exception: Exception) -> Unit,
    private val handlerContainer: MsgHandlerContainer,
    private val externalScope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val startTimeOut:Long=5000
) : IOPubListenerService {

    internal constructor(
        kernelContext: KernelContext,
        defaultHandler: (msg: JPRawMessage) -> Unit = { /*do nothing*/ },
        parseExceptionHandler: suspend (exception: Exception) -> Unit = {  /*do nothing*/ },
        handlerContainer: MsgHandlerContainer = HandlerContainerImp(),
        externalScope: CoroutineScope,
        dispatcher: CoroutineDispatcher,
    ) : this(
        kernelContext.conv(), defaultHandler, parseExceptionHandler, handlerContainer, externalScope, dispatcher
    )

    private var job: Job? = null

    /**
     * this will start this listener on a coroutine that runs concurrently.
     */
    override suspend fun start(): Result<Unit, Exception> {

        if (this.isRunning()) {
            return Ok(Unit)
        }

        if(kernelContext.isKernelRunning().not()){
            return Err(KernelIsDownException.occurAt(this))
        }

        val hbIsAlive:Boolean = kernelContext.getConvHeartBeatService().get()?.isHBAlive() ?: false

        if (hbIsAlive.not()) {
            return Err(HBIsDeadException(ExceptionInfo(loc = this, data = Unit)))
        }

        val socket: ZMQ.Socket = kernelContext.getSocketProvider().unwrap().ioPubSocket()
        // x: add default handler
        this.addDefaultHandler(MsgHandlers.withUUID(MsgType.DEFAULT, defaultHandler))
        job = externalScope.launch(dispatcher) {
            socket.use {
                // x: start the service loop
                // x: when the kernel is down, this service simply does not do anything. Just hang there.
                while (isActive) {
                    if (kernelContext.getConvHeartBeatService().get()?.isHBAlive() == true) {
                        val msg = ZMsg.recvMsg(it, ZMQ.DONTWAIT)
                        if (msg != null) {
                            val parseResult = JPRawMessage.fromPayload(msg.map { f -> f.data })
                            when (parseResult) {
                                is Ok -> {
                                    val rawMsg: JPRawMessage = parseResult.unwrap()
                                    val msgType: MsgType = extractMsgType(rawMsg.identities)
                                    dispatch(msgType, rawMsg)
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
             return Err(CantStartIOPubServiceException(ExceptionInfo(
                msg = "Time out when trying to start IOPub service",
                loc = this,
                data = "timeout"
            )))
        }
        return waitRs
    }

    private fun extractMsgType(msgIdentity: String): MsgType {
        val msgType: MsgType = when {

            msgIdentity.endsWith(IOPub.ExecuteResult.msgType.text()) -> IOPub.ExecuteResult.msgType

            msgIdentity.endsWith(IOPub.Status.msgType.text()) -> IOPub.Status.msgType

            msgIdentity.endsWith(IOPub.ExecuteError.getMsgType2().text()) -> IOPub.ExecuteError.getMsgType2()

            // TODO add more msg type here

            else -> {
                MsgType.DEFAULT
            }
        }
        return msgType
    }

    override suspend fun stop(): Result<Unit, Exception> {
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

    private fun dispatch(
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
