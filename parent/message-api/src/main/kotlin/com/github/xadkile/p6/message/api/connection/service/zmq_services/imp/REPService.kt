package com.github.xadkile.p6.message.api.connection.service.zmq_services.imp

import com.github.xadkile.p6.message.api.connection.kernel_context.KernelContextReadOnly
import com.github.xadkile.p6.message.api.connection.service.zmq_services.ZMQSocketListenerServiceImp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import org.zeromq.SocketType
import org.zeromq.ZMQ
import org.zeromq.ZMsg

internal class REPService (
    private val kernelContext: KernelContextReadOnly,
    coroutineScope: CoroutineScope,
    coroutineDispatcher: CoroutineDispatcher,
) : ZMQSocketListenerServiceImp(coroutineScope,coroutineDispatcher) {


    override fun makeSocket(): ZMQ.Socket {
        val zcontext = kernelContext.zContext()
        val socket = zcontext.createSocket(SocketType.REP)
        socket.bind("tcp://localhost:${this.zmqPort}")
        return socket
    }

    override fun receiveMessage(socket: ZMQ.Socket) {
        val msg: ZMsg? = ZMsg.recvMsg(socket)
        if (msg!=null){
            val data:List<ByteArray> = msg.map { it.data }
            listenerMap.values.forEach {
                it.handleMessage(data)
            }
        }
    }
    override val socketType: SocketType = SocketType.REP
}
