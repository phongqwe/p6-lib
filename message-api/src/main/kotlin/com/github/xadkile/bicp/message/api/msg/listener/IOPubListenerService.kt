package com.github.xadkile.bicp.message.api.msg.listener

import com.github.michaelbull.result.*
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
class IOPubListenerService constructor(
    private val socketProvider: SocketProvider,
    private val cScope: CoroutineScope,
    private val cDispatcher: CoroutineDispatcher,
    private val defaultHandler: MsgHandler = MsgHandlers.default,
    private val parseExceptionHandler: (exception:Exception) -> Unit = {
       println(it)
    },
) : MsgListenerService {

    private val handlerContainer: MsgHandlerContainer = HandlerContainerImp()
    private var job: Job? = null

    init {
        this.addHandler(defaultHandler)
    }

    override fun start() {
        this.job = cScope.launch(cDispatcher) {
            val iopubSocket: ZMQ.Socket = socketProvider.ioPubSocket()
            iopubSocket.use {
                while (isActive) {
                    val msg = ZMsg.recvMsg(iopubSocket, ZMQ.DONTWAIT)
                    if (msg != null) {
                        val rawMsgResult = JPRawMessage.fromPayload(msg.map { it.data })
                        when(rawMsgResult){
                            is Ok ->{
                                val rawMsg = rawMsgResult.unwrap()
                                val msgType = if (rawMsg.identities.contains("execute_result")) {
                                    IOPub.ExecuteResult.msgType
                                } else {
                                    MsgType.DONT_EXIST
                                }
                                dispatch(msgType, rawMsg)
                            }
                            else ->{
                                parseExceptionHandler(rawMsgResult.unwrapError())
                            }
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
}
