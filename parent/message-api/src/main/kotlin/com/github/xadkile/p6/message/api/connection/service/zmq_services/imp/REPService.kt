package com.github.xadkile.p6.message.api.connection.service.zmq_services.imp

import com.github.xadkile.p6.message.api.connection.kernel_context.KernelContextReadOnly
import com.github.xadkile.p6.message.api.connection.service.zmq_services.AbstractZMQSocketService
import com.github.xadkile.p6.message.api.connection.service.zmq_services.ZMQSocketListenerService
import com.github.xadkile.p6.message.api.connection.service.zmq_services.msg.P6Message
import com.github.xadkile.p6.message.api.message.protocol.ProtocolUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import org.zeromq.SocketType
import org.zeromq.ZMQ
import org.zeromq.ZMsg

internal class REPService (
    private val kernelContext: KernelContextReadOnly,
    coroutineScope: CoroutineScope,
    coroutineDispatcher: CoroutineDispatcher,
) : AbstractZMQSocketService(coroutineScope,coroutineDispatcher), ZMQSocketListenerService {

    override fun makeSocket(): ZMQ.Socket {
        val zcontext = kernelContext.zContext()
        val socket = zcontext.createSocket(SocketType.REP)
        socket.bind("tcp://*:${this.zmqPort}")
        return socket
    }

    override fun receiveMessage(socket: ZMQ.Socket) {
        val msg: ZMsg? = ZMsg.recvMsg(socket,ZMQ.DONTWAIT)
        if (msg!=null){
            val dataStr: String = msg.joinToString("") { String(it.data) }
            val p6Msg:P6Message = ProtocolUtils.msgGson.fromJson(dataStr,P6Message::class.java)
            val handlers = this.listenerMap[p6Msg.header.msgType]?.values ?: emptyList()
            for(handler in handlers){
                handler.handleMessage(p6Msg)
            }
            // x: send a reply when all handlers finish running
            socket.send("ok")
        }
    }
    override val socketType: SocketType = SocketType.REP
}
