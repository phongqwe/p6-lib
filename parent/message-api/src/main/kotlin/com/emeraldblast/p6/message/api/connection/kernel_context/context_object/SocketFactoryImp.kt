package com.emeraldblast.p6.message.api.connection.kernel_context.context_object

import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ

class SocketFactoryImp @AssistedInject constructor(
    @Assisted private val channelProvider: ChannelProvider,
    @Assisted private val zContext: ZContext
) : SocketFactory {

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
        ioPubSocket.connect(this.channelProvider.ioPubAddress())
        ioPubSocket.subscribe("")
        return ioPubSocket
    }

    override fun controlSocket(): ZMQ.Socket {
        TODO("Not yet implemented")
    }
}
