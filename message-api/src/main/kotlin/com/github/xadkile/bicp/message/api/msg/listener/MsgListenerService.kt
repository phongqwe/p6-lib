package com.github.xadkile.bicp.message.api.msg.listener

import com.github.michaelbull.result.unwrap
import com.github.xadkile.bicp.message.api.connection.ipython_context.SocketProvider
import com.github.xadkile.bicp.message.api.protocol.message.JPRawMessage
import com.github.xadkile.bicp.message.api.protocol.message.MsgType
import com.github.xadkile.bicp.message.api.protocol.message.data_interface_definition.IOPub
import kotlinx.coroutines.*
import org.zeromq.ZMQ
import org.zeromq.ZMsg
import kotlin.concurrent.thread
import kotlin.coroutines.coroutineContext

/**
 * Listen for in-coming message.
 * Dispatch message to the appropriate handlers.
 */
interface MsgListenerService {
    fun start()
    fun stop()
    fun addHandler(mgsType: MsgType, handler: MsgHandler)
    fun removeHandler(handlerId: String)
}

class IOPubListenerService(val socketProvider: SocketProvider) : MsgListenerService {
    val handlerContainer: MsgHandlerContainer = HandlerContainerImp()
    var job:Job?= null

    @OptIn(DelicateCoroutinesApi::class)
    override fun start() {
        this.addHandler(MsgType.IOPub_execute_result, object : MsgHandler {
            override fun handle(msg: JPRawMessage) {
                val md = msg.toModel<IOPub.ExecuteResult.MetaData, IOPub.ExecuteResult.Content>()
                println(md)
            }
            override fun id(): String {
                return "1"
            }
        })

        this.job = GlobalScope.launch {
            val subSocket: ZMQ.Socket = socketProvider.ioPubSocket()
            subSocket.use {
                while (isActive) {
                    val msg = ZMsg.recvMsg(subSocket)
                    val rawMsg = JPRawMessage.fromPayload(msg.map { it.data }).unwrap()
                    if (rawMsg.identities.contains("execute_result")) {
                        dispatch(MsgType.IOPub_execute_result, rawMsg)
                    } else {
                        println(rawMsg)
                    }
                }
            }
        }
    }

    override fun stop() {
        job?.cancel()
    }

    private fun dispatch(msgType: MsgType, msg: JPRawMessage) {
        handlerContainer.getHandler(msgType).forEach {

            it.handle(msg)
        }
    }

    override fun addHandler(msgType: MsgType, handler: MsgHandler) {
        this.handlerContainer.addHandler(msgType,handler)
    }

    override fun removeHandler(handlerId: String) {
        TODO("Not yet implemented")
    }
}

