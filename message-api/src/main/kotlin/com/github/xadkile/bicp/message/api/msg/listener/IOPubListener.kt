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
 * Listen to pub msg from IOPub channel
 * Dispatch msg to appropriate handlers.
 * [defaultHandler] to handle DONT_EXIST msg type
 * [parseExceptionHandler] to handle exception of unable to parse zmq message
 */
class IOPubListener constructor(
    private val kernelContext:KernelContextReadOnlyConv,
    private val cScope: CoroutineScope=GlobalScope,
    private val cDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val defaultHandler: (msg: JPRawMessage) -> Unit = {},
    private val parseExceptionHandler: (exception: Exception) -> Unit = { /*do nothing*/ },
) : MsgListener {

    private val handlerContainer: MsgHandlerContainer = HandlerContainerImp()
    private var job: Job? = null
    private var internalRunning = true
    val socketProvider: SocketProvider = kernelContext.getSocketProvider().unwrap()
    /**
     * I have 3 way to write this listener
     * 1. I can write it bare, don't use any coroutine or anything. State is managed internally
     *  => must make effort to launch it on the correct scope
     * 2. Write with baked in coroutine
     *      + with injected scope
     *      + with auto inherited scope
     */
    override fun start() {
        this.job = cScope.launch(cDispatcher) {
            val iopubSocket: ZMQ.Socket = socketProvider.ioPubSocket()
            iopubSocket.use {
                while (isActive) {
                    val msg = ZMsg.recvMsg(iopubSocket, ZMQ.DONTWAIT)
                    if (msg != null) {
                        val rawMsgResult = JPRawMessage.fromPayload(msg.map { it.data })
                        when (rawMsgResult) {
                            is Ok -> {
                                val rawMsg = rawMsgResult.unwrap()
                                val identity = rawMsg.identities
                                val msgType = if (identity.endsWith("execute_result")) {
                                    IOPub.ExecuteResult.msgType
                                } else if (identity.endsWith("status")) {
                                    IOPub.Status.msgType
                                } else {
                                    MsgType.NOT_RECOGNIZE
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

    suspend fun startSus() {
        coroutineScope {
            job = launch(cDispatcher) {
                val iopubSocket: ZMQ.Socket = socketProvider.ioPubSocket()
                iopubSocket.use {
                    while (isActive) {
                        val msg = ZMsg.recvMsg(iopubSocket, ZMQ.DONTWAIT)
                        if (msg != null) {
                            val rawMsgResult = JPRawMessage.fromPayload(msg.map { it.data })
                            when (rawMsgResult) {
                                is Ok -> {
                                    val rawMsg = rawMsgResult.unwrap()
                                    val identity = rawMsg.identities
                                    val msgType = if (identity.endsWith("execute_result")) {
                                        IOPub.ExecuteResult.msgType
                                    } else if (identity.endsWith("status")) {
                                        IOPub.Status.msgType
                                    } else {
                                        MsgType.NOT_RECOGNIZE
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

    fun startBare() {

        val iopubSocket: ZMQ.Socket = socketProvider.ioPubSocket()
        this.internalRunning = true

        iopubSocket.use {
            while (this.internalRunning) {
                val msg = ZMsg.recvMsg(iopubSocket, ZMQ.DONTWAIT)
                if (msg != null) {
                    val rawMsgResult = JPRawMessage.fromPayload(msg.map { it.data })
                    when (rawMsgResult) {
                        is Ok -> {
                            val rawMsg = rawMsgResult.unwrap()
                            val identity = rawMsg.identities
                            val msgType = if (identity.endsWith("execute_result")) {
                                IOPub.ExecuteResult.msgType
                            } else if (identity.endsWith("status")) {
                                IOPub.Status.msgType
                            } else {
                                MsgType.NOT_RECOGNIZE
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


    override fun stop() {

        runBlocking {
            job?.cancelAndJoin()
        }
        this.job = null

    }

    fun stopII(){
        this.internalRunning = false
    }

    override fun isRunning(): Boolean {
        return this.job?.isActive == true
    }

    private fun dispatch(msgType: MsgType, msg: JPRawMessage) {
        handlerContainer.getHandlers(msgType).forEach {
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
        this.stop()
    }
}
