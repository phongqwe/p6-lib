package com.github.xadkile.bicp.message.api.msg.listener

import com.github.michaelbull.result.unwrap
import com.github.xadkile.bicp.message.api.connection.ipython_context.SocketProvider
import com.github.xadkile.bicp.message.api.protocol.message.JPRawMessage
import com.github.xadkile.bicp.message.api.protocol.message.MsgType
import kotlinx.coroutines.*
import org.zeromq.ZMQ
import org.zeromq.ZMsg

/**
 * Listen to pub msg from IOPub channel
 * Dispatch msg to appropriate handlers.
 */
class IOPubListenerService constructor(
    private val socketProvider: SocketProvider,
    private val cScope: CoroutineScope,
    private val cDispatcher: CoroutineDispatcher,
) : MsgListenerService {

    private val handlerContainer: MsgHandlerContainer = HandlerContainerImp()
    private var job: Job? = null

    override fun start() {
        this.job = cScope.launch(cDispatcher) {
            val iopubSocket: ZMQ.Socket = socketProvider.ioPubSocket()
            iopubSocket.use {
                while (isActive) {
                    val msg = ZMsg.recvMsg(iopubSocket, ZMQ.DONTWAIT)
                    if (msg != null) {
                        val rawMsg = JPRawMessage.fromPayload(msg.map { it.data }).unwrap()
                        if (rawMsg.identities.contains("execute_result")) {
                            dispatch(MsgType.IOPub_execute_result, rawMsg)
                        } else {
                            println(rawMsg.identities)
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
