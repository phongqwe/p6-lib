package com.github.xadkile.p6.message.api.connection.service.zmq_services.imp

import com.github.xadkile.message.api.proto.P6MsgPM
import com.github.xadkile.p6.message.api.connection.kernel_context.KernelContextReadOnly
import com.github.xadkile.p6.message.api.connection.service.zmq_services.AbstractZMQService
import com.github.xadkile.p6.message.api.connection.service.zmq_services.ZMQListenerService
import com.github.xadkile.p6.message.api.connection.service.zmq_services.msg.P6Message
import com.github.xadkile.p6.message.api.connection.service.zmq_services.msg.toModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zeromq.SocketType
import org.zeromq.ZMQ
import org.zeromq.ZMsg

internal class REPServiceProto(
    private val kernelContext: KernelContextReadOnly,
    coroutineScope: CoroutineScope,
    coroutineDispatcher: CoroutineDispatcher,
    val logger: Logger?=null,
) : AbstractZMQService(coroutineScope, coroutineDispatcher), ZMQListenerService {


    override fun makeSocket(): ZMQ.Socket {
        val zcontext = kernelContext.zContext()
        val socket = zcontext.createSocket(SocketType.REP)
        socket.bind("tcp://*:${this.zmqPort}")
        return socket
    }

    override fun receiveMessage(socket: ZMQ.Socket) {
        logger?.info("start receive msg")
        try {
            val msg: ZMsg? = ZMsg.recvMsg(socket)
            if (msg != null) {
                logger?.info("receive msg ok")
                val dataStr: String = msg.joinToString("") { String(it.data) }
                val p6MsgProto =
                    P6MsgPM.P6MessageProto.newBuilder()
                        .mergeFrom(dataStr.toByteArray())
                        .build()
                logger?.debug(p6MsgProto.toString())
                val p6Msg: P6Message = p6MsgProto.toModel()
                logger?.debug(p6Msg.toString())
                val handlers = this.getHandlerByMsgType(p6Msg.header.eventType)
                for (handler in handlers) {
                    handler.handleMessage(p6Msg)
                }
                // x: send a reply when all handlers finish running
                socket.send("ok")
            }else{
                logger?.debug("msg null")
                socket.send("fail")
            }
        } catch (e: Exception) {
            // receiver service must not crash
            socket.send("fail")
            logger?.error(e.toString())
        }
    }

    override val socketType: SocketType = SocketType.REP
}
