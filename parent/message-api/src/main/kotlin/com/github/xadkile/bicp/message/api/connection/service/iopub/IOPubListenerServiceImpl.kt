package com.github.xadkile.bicp.message.api.connection.service.iopub

import com.github.michaelbull.result.*
import com.github.xadkile.bicp.message.api.connection.kernel_context.*
import com.github.xadkile.bicp.message.api.connection.kernel_context.exception.KernelIsDownException
import com.github.xadkile.bicp.message.api.msg.protocol.JPRawMessage
import com.github.xadkile.bicp.message.api.msg.protocol.MsgType
import com.github.xadkile.bicp.message.api.msg.protocol.data_interface_definition.IOPub
import com.github.xadkile.bicp.message.api.other.Sleeper
import kotlinx.coroutines.*
import org.zeromq.ZMQ
import org.zeromq.ZMsg

/**
 * Listen to pub msg from IOPub channel and dispatch msg to appropriate handlers.
 * [defaultHandler] to handle msg type that don't have a specific handler.
 * [parseExceptionHandler] to handle exception of unable to parse zmq message.
 */
class IOPubListenerServiceImpl constructor(
    private val kernelContext: KernelContextReadOnlyConv,
    private val defaultHandler: (msg: JPRawMessage) -> Unit,
    private val parseExceptionHandler: suspend (exception: Exception) -> Unit,
    private val handlerContainer: MsgHandlerContainer,
    private val externalScope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
) : IOPubListenerService {

    constructor(
        kernelContext: KernelContext,
        defaultHandler: (msg: JPRawMessage) -> Unit = { /*do nothing*/ },
        parseExceptionHandler: suspend (exception: Exception) -> Unit = {  /*do nothing*/ },
        handlerContainer: MsgHandlerContainer = HandlerContainerImp(),
        externalScope: CoroutineScope,
        dispatcher: CoroutineDispatcher,
    ) : this(
        kernelContext.conv(), defaultHandler, parseExceptionHandler, handlerContainer,externalScope,dispatcher
    )

    private var job: Job? = null

    /**
     * this will start this listener on a coroutine that runs concurrently.
     */
    override fun start(): Result<Unit, Exception> {

        if (this.isRunning()) {
            return Ok(Unit)
        }

        if (this.kernelContext.isNotRunning()) {
            return Err(KernelIsDownException.occurAt(this))
        }

        val socket: ZMQ.Socket = kernelContext.getSocketProvider().unwrap().ioPubSocket()
        // add default handler
        addHandler(MsgHandlers.withUUID(MsgType.NOT_RECOGNIZE, defaultHandler))
        job = externalScope.launch(dispatcher) {
            socket.use {
                // p: start the service loop
                // ph: when the kernel is down, this service simply does not do anything. Just hang there.
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
        Sleeper.waitUntil { this.isRunning() }
        return Ok(Unit)
    }

    private fun extractMsgType(msgIdentity: String): MsgType {
        val msgType: MsgType = when {

            msgIdentity.endsWith(IOPub.ExecuteResult.msgType.text()) -> IOPub.ExecuteResult.msgType

            msgIdentity.endsWith(IOPub.Status.msgType.text()) -> IOPub.Status.msgType

            msgIdentity.endsWith(IOPub.ExecuteError.getMsgType2().text()) -> IOPub.ExecuteError.getMsgType2()

            // TODO add more msg type here

            else -> {
                MsgType.NOT_RECOGNIZE
            }
        }
        return msgType
    }

    override suspend fun stop() {
        if (this.isRunning()) {
            job?.cancelAndJoin()
            this.job = null
        }
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
