package com.github.xadkile.bicp.message.api.connection.ipython_context

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
        TODO("Not yet implemented")
    }

    override fun controlSocket(): ZMQ.Socket {
        TODO("Not yet implemented")
    }
}
