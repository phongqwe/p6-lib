package com.github.xadkile.bicp.message.api.msg.listener

import com.github.michaelbull.result.*
import com.github.xadkile.bicp.message.api.connection.kernel_context.*
import com.github.xadkile.bicp.message.api.msg.protocol.message.JPRawMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.MsgType
import com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition.IOPub
import kotlinx.coroutines.*
import org.zeromq.ZMQ
import org.zeromq.ZMsg

/**
 * Listen to pub msg from IOPub channel.
 * Dispatch msg to appropriate handlers.
 * [defaultHandler] to handle DONT_EXIST msg type
 * [parseExceptionHandler] to handle exception of unable to parse zmq message.
 * When this is running, and the kernel is killed, heart beat does not ping anymore. What should I do?
 * Normally, I would like to:
 *  - notify all handler that something is wrong, and they should do something about it.
 *      => need to extend the handler interface to handle
 *
 */
class IOPubListener constructor(
    private val kernelContext: KernelContextReadOnlyConv,
    private val defaultHandler: suspend (msg: JPRawMessage, listener: IOPubListener) -> Unit,
    private val parseExceptionHandler: suspend (exception: Exception, listener: IOPubListener) -> Unit,
    private val parallelHandler: Boolean,
    private val handlerContainer: MsgHandlerContainer,
) : MsgListener {

    constructor(
        kernelContext: KernelContext,
        defaultHandler: suspend (msg: JPRawMessage, listener: IOPubListener) -> Unit = { _, _ -> /*do nothing*/ },
        parseExceptionHandler: suspend (exception: Exception, listener: IOPubListener) -> Unit = { _, _ -> /*do nothing*/ },
        parallelHandler: Boolean = true,
        handlerContainer: MsgHandlerContainer = HandlerContainerImp(),
    ) : this(
        kernelContext.conv(), defaultHandler, parseExceptionHandler, parallelHandler, handlerContainer
    )

    private var job: Job? = null

    /**
     * TODO how to handle exception when kernel context not running
     * this will start this listener on a coroutine that runs concurrently, in parallel, or whatnot.
     */
    override suspend fun start(
        externalScope: CoroutineScope,
        dispatcher: CoroutineDispatcher,
    ): Result<Unit, Exception> {
        return this.checkContextRunningThen {
            job = externalScope.launch(dispatcher) {
                kernelContext.getSocketProvider().unwrap().ioPubSocket().use {
                    while (isActive) {
                        if (kernelContext.getConvHeartBeatService().unwrap().isHBAlive()) {
                            val msg = ZMsg.recvMsg(it, ZMQ.DONTWAIT)
                            if (msg != null) {
                                when (val rawMsgResult = JPRawMessage.fromPayload(msg.map { f -> f.data })) {
                                    is Ok -> {
                                        val rawMsg = rawMsgResult.unwrap()
                                        val identity = rawMsg.identities
                                        val msgType = when {
                                            identity.endsWith("execute_result") -> {
                                                IOPub.ExecuteResult.msgType
                                            }
                                            identity.endsWith("status") -> {
                                                IOPub.Status.msgType
                                            }
                                            else -> {
                                                MsgType.NOT_RECOGNIZE
                                            }
                                        }
                                        if (msgType == MsgType.NOT_RECOGNIZE) {
                                            defaultHandler(rawMsg, this@IOPubListener)
                                        } else {
                                            dispatch(msgType, rawMsg, dispatcher)
                                        }
                                    }
                                    else -> {
                                        parseExceptionHandler(rawMsgResult.unwrapError(), this@IOPubListener)
                                    }
                                }
                            }
                        } else {
                            reactOnKernelDown(dispatcher)
                        }
                    }
                }
            }
            Ok(Unit)
        }
    }

    private suspend fun reactOnKernelDown(dispatcher: CoroutineDispatcher) {
        coroutineScope {
            // p: heart beat is not pinging
            val exception = KernelIsDownException.occurAt(this@IOPubListener)
            allHandlers().forEach {
                if (parallelHandler) {
                    launch(dispatcher) {
                        it.onListenerException(exception, this@IOPubListener)
                    }
                } else {
                    it.onListenerException(exception, this@IOPubListener)
                }
            }
        }
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
}
