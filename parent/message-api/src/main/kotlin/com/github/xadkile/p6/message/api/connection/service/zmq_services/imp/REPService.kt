package com.github.xadkile.p6.message.api.connection.service.zmq_services.imp

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.xadkile.p6.common.exception.error.ErrorReport
import com.github.xadkile.p6.message.api.connection.kernel_context.KernelContextReadOnly
import com.github.xadkile.p6.message.api.connection.service.zmq_services.AbstractZMQService
import com.github.xadkile.p6.message.api.connection.service.zmq_services.P6MessageHandler
import com.github.xadkile.p6.message.api.connection.service.zmq_services.ZMQListenerService
import com.github.xadkile.p6.message.api.connection.service.zmq_services.msg.P6Message
import com.github.xadkile.p6.message.api.message.protocol.ProtocolUtils
import kotlinx.coroutines.*
import org.zeromq.SocketType
import org.zeromq.ZMQ
import org.zeromq.ZMsg

internal class REPService (
    private val kernelContext: KernelContextReadOnly,
    coroutineScope: CoroutineScope,
    coroutineDispatcher: CoroutineDispatcher,
) : AbstractZMQService<P6Message>(coroutineScope,coroutineDispatcher), ZMQListenerService<P6Message> {

    override fun makeSocket(): ZMQ.Socket {
        val zcontext = kernelContext.zContext()
        val socket = zcontext.createSocket(SocketType.REP)
        socket.bind("tcp://*:${this.zmqPort}")
        return socket
    }

    override fun receiveMessage(socket: ZMQ.Socket) {
        // Without DONTWAIT, this service may not be able to stop. Each while loop will wait until it receive a message. So, if I issue stop after the loop start, the stop effect will not be taken into effect after another receive a message is received.
        val msg: ZMsg? = ZMsg.recvMsg(socket)
//        val msg: ZMsg? = ZMsg.recvMsg(socket,ZMQ.DONTWAIT)
        if (msg!=null){
            val dataStr: String = msg.joinToString("") { String(it.data) }
            val p6Msg:P6Message = ProtocolUtils.msgGson.fromJson(dataStr,P6Message::class.java)
            val handlers = this.getHandlerByMsgType(p6Msg.header.eventType)
            for(handler in handlers){
                handler.handleMessage(p6Msg)
            }
            // x: send a reply when all handlers finish running
            socket.send("ok")
        }
    }
    override val socketType: SocketType = SocketType.REP
}
