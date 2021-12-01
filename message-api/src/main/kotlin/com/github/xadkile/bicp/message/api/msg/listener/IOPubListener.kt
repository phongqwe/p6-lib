package com.github.xadkile.bicp.message.api.msg.listener

import com.github.michaelbull.result.*
import com.github.xadkile.bicp.message.api.connection.kernel_context.*
import com.github.xadkile.bicp.message.api.msg.protocol.message.JPRawMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgType
import com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition.IOPub
import com.github.xadkile.bicp.message.api.other.Sleeper
import com.github.xadkile.bicp.message.api.system.SystemEvent
import kotlinx.coroutines.*
import org.zeromq.ZMQ
import org.zeromq.ZMsg

/**
 * Listen to pub msg from IOPub channel and dispatch msg to appropriate handlers.
 * [defaultHandler] to handle msg type that don't have a specific handler.
 * [parseExceptionHandler] to handle exception of unable to parse zmq message.
 */
class IOPubListener constructor(
    private val kernelContext: KernelContextReadOnlyConv,
    private val defaultHandler: suspend (msg: JPRawMessage, listener: MsgListener) -> Unit,
    private val parseExceptionHandler: suspend (exception: Exception, listener: IOPubListener) -> Unit,
    private val parallelHandler: Boolean,
    private val handlerContainer: MsgHandlerContainer,
    private val ignoreKernelRunningStatus:Boolean=false
) : MsgListener {

    constructor(
        kernelContext: KernelContext,
        defaultHandler: suspend (msg: JPRawMessage, listener: MsgListener) -> Unit = { _, _ -> /*do nothing*/ },
        parseExceptionHandler: suspend (exception: Exception, listener: IOPubListener) -> Unit = { _, _ -> /*do nothing*/ },
        parallelHandler: Boolean = true,
        handlerContainer: MsgHandlerContainer = HandlerContainerImp(),
    ) : this(
        kernelContext.conv(), defaultHandler, parseExceptionHandler, parallelHandler, handlerContainer
    )

    private var job: Job? = null

    /**
     * this will start this listener on a coroutine that runs concurrently.
     */
    override suspend fun start(
        externalScope: CoroutineScope,
        dispatcher: CoroutineDispatcher,
    ): Result<Unit, Exception> {
        if (this.kernelContext.isNotRunning()) {
            return Err(KernelIsDownException.occurAt(this))
        }
        job = externalScope.launch(dispatcher) {
            kernelContext.getSocketProvider().unwrap().ioPubSocket().use {
                // add default handler
                addHandler(MsgHandlers.withUUID(MsgType.NOT_RECOGNIZE, defaultHandler))

                // p: start the service loop
                // ph: when the kernel is down, this service simply does not do anything. Just hang there.
                while (isActive) {
                    if (kernelContext.getConvHeartBeatService().unwrap().isHBAlive()) {
                        val msg = ZMsg.recvMsg(it, ZMQ.DONTWAIT)
                        if (msg != null) {
                            val parseResult = JPRawMessage.fromPayload(msg.map { f -> f.data })
                            when (parseResult) {
                                is Ok -> {
                                    val rawMsg: JPRawMessage = parseResult.unwrap()
                                    val msgType: MsgType = extractMsgType(rawMsg.identities)
                                    dispatch(msgType, rawMsg, dispatcher)
                                }
                                else -> {
                                    parseExceptionHandler(parseResult.unwrapError(), this@IOPubListener)
                                }
                            }
                        }
                    }
                }
            }
        }
        Sleeper.waitUntil { job?.isActive == true}
        return Ok(Unit)
    }

    private fun extractMsgType(msgIdentity: String): MsgType {
        val msgType: MsgType = when {

            msgIdentity.endsWith(IOPub.ExecuteResult.msgType.text()) -> IOPub.ExecuteResult.msgType

            msgIdentity.endsWith(IOPub.Status.msgType.text()) -> IOPub.Status.msgType

            msgIdentity.endsWith(IOPub.Error.getMsgType2().text()) -> IOPub.Error.getMsgType2()

            // TODO add more msg type here

            else -> {
                println("msg type not recog: $msgIdentity")
                MsgType.NOT_RECOGNIZE
            }
        }
        return msgType
    }


    override suspend fun stop() {
        job?.cancelAndJoin()
        this.job = null
    }

    override fun isRunning(): Boolean {
        return this.job?.isActive == true
    }

    private suspend fun dispatch(msgType: MsgType, msg: JPRawMessage, dispatcher: CoroutineDispatcher) {
        if (parallelHandler) {
            supervisorScope {
                handlerContainer.getHandlers(msgType).forEach {
                    launch(dispatcher) { it.handle(msg, this@IOPubListener) }
                }
            }
        } else {
            handlerContainer.getHandlers(msgType).forEach {
                it.handle(msg, this)
            }
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
        this.removeHandler(handler)
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

    override fun close() {
        runBlocking {
            stop()
        }
    }

    override fun getKernelContext(): KernelContextReadOnly {
        return this.kernelContext
    }

    override fun onSystemEvent(event: SystemEvent) {
        TODO("Not yet implemented")
    }

}
