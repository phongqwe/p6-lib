package com.github.xadkile.bicp.message.api.msg.listener

import com.github.michaelbull.result.*
import com.github.xadkile.bicp.message.api.connection.ipython_context.KernelContextReadOnlyConv
import com.github.xadkile.bicp.message.api.connection.ipython_context.SocketProvider
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
 * Now, the question is: should I allow suspend function in interface, or should I only write raw logic code and w
 * manual state handling or depend on coroutine ????????
 * manual state handling is dangerous for sure.
 * But...manual state allow a clean state
 *
 *
 */
class IOPubListener constructor(
    private val kernelContext: KernelContextReadOnlyConv,
//    private val externalScope:CoroutineScope,
//    private val cDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val defaultHandler: (msg: JPRawMessage) -> Unit = {},
    private val parseExceptionHandler: (exception: Exception) -> Unit = { /*do nothing*/ },
    private val parallelHandler:Boolean = false
) : MsgListener {

    private val handlerContainer: MsgHandlerContainer = HandlerContainerImp()
    private var job: Job? = null
    private val socketProvider: SocketProvider = kernelContext.getSocketProvider().unwrap()

    override suspend fun start(externalScope:CoroutineScope,cDispatcher: CoroutineDispatcher) {
        withContext(cDispatcher) {
            job = externalScope.launch {
                socketProvider.ioPubSocket().use {
                    while (isActive) {
                        val msg = ZMsg.recvMsg(it, ZMQ.DONTWAIT)
                        if (msg != null) {
                            when (val rawMsgResult = JPRawMessage.fromPayload(msg.map { f->f.data })) {
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
                                        defaultHandler(rawMsg)
                                    } else {
                                        dispatch(msgType, rawMsg)
                                    }
                                }
                                else -> {
                                    parseExceptionHandler(rawMsgResult.unwrapError())
                                }
                            }
                        }
                    }
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

    private suspend fun dispatch(msgType: MsgType, msg: JPRawMessage) {
        if(parallelHandler){
            supervisorScope {
                handlerContainer.getHandlers(msgType).forEach {
                    launch{it.handle(msg)}
                }
            }
        }else{
            handlerContainer.getHandlers(msgType).forEach {
                it.handle(msg)
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

    override fun removeHandler(msgType: MsgType, handlerId: String) {
        this.removeHandler(msgType, handlerId)
    }

    override fun removeHandler(handler: MsgHandler) {
        this.removeHandler(handler)
    }

    override val entries: Set<Map.Entry<MsgType, List<MsgHandler>>>
        get() = this.handlerContainer.entries
    override val keys: Set<MsgType>
        get() = this.handlerContainer.keys
    override val size: Int
        get() = this.handlerContainer.size
    override val values: Collection<List<MsgHandler>>
        get() = this.handlerContainer.values

    override fun containsKey(key: MsgType): Boolean {
        return this.handlerContainer.containsKey(key)
    }

    override fun containsValue(value: List<MsgHandler>): Boolean {
        return this.handlerContainer.containsValue(value)
    }

    override fun get(key: MsgType): List<MsgHandler>? {
        return this.handlerContainer.get(key)
    }

    override fun isEmpty(): Boolean {
        return this.handlerContainer.isEmpty()
    }

    override fun close() {
        runBlocking {
            stop()
        }
    }
}
