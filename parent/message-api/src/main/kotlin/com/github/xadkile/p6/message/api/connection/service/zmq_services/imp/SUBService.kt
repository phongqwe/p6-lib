package com.github.xadkile.p6.message.api.connection.service.zmq_services.imp

import com.github.xadkile.p6.message.api.connection.kernel_context.KernelContextReadOnly
import com.github.xadkile.p6.message.api.connection.service.zmq_services.AbstractZMQService
import com.github.xadkile.p6.message.api.connection.service.zmq_services.msg.P6Message
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import org.zeromq.SocketType
import org.zeromq.ZMQ
import org.zeromq.ZMsg

/**
 * A service that hosts a SUB socket
 */
internal class SUBService(
    private val kernelContext: KernelContextReadOnly,
    coroutineScope: CoroutineScope,
    coroutineDispatcher: CoroutineDispatcher,
) : AbstractZMQService<P6Message>(coroutineScope,coroutineDispatcher) {


    override fun makeSocket(): ZMQ.Socket {
        val zcontext = kernelContext.zContext()
        val socket = zcontext.createSocket(SocketType.SUB)
        socket.connect("tcp://localhost:${this.zmqPort}")
        return socket
    }

    override fun receiveMessage(socket: ZMQ.Socket) {
        val msg:ZMsg? = ZMsg.recvMsg(socket)
        if (msg!=null){
            val data:List<ByteArray> = msg.map { it.data }
            // TODO
        }
    }
    override val socketType: SocketType = SocketType.SUB
}
