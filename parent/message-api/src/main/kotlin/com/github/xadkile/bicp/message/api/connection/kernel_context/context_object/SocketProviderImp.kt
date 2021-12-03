package com.github.xadkile.bicp.message.api.connection.kernel_context.context_object

import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ

internal class SocketProviderImp(
    private val channelProvider: ChannelProvider,
    private val zContext: ZContext
) : SocketProvider {

    override fun shellSocket(): ZMQ.Socket {
        return this.zContext.createSocket(SocketType.REQ).also {
            it.connect(this.channelProvider.shellAddress())
        }
    }

    override fun heartBeatSocket(): ZMQ.Socket {
        return this.zContext.createSocket(SocketType.REQ).also {
            it.connect(this.channelProvider.heartBeatAddress())
        }
    }

    override fun ioPubSocket(): ZMQ.Socket {
        val ioPubSocket: ZMQ.Socket = this.zContext.createSocket(SocketType.SUB)
        ioPubSocket.connect(this.channelProvider.ioPUBAddress())
        ioPubSocket.subscribe("")
        return ioPubSocket
    }

    override fun controlSocket(): ZMQ.Socket {
        TODO("Not yet implemented")
    }
}
