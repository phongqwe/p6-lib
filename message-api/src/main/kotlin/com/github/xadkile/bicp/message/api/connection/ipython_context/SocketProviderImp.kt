package com.github.xadkile.bicp.message.api.connection.ipython_context

import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ

internal class SocketProviderImp(
    private val channelProvider: ChannelProvider,
    private val zContext: ZContext
) : SocketProvider {

    private val shell: ZMQ.Socket by lazy {
        this.newShellSocket()
    }
    override fun shellSocket(): ZMQ.Socket {
        return shell
    }
    override fun newShellSocket(): ZMQ.Socket {
        return this.zContext.createSocket(SocketType.REQ).also {
            it.connect(this.channelProvider.shellAddress())
        }
    }

    private val hb: ZMQ.Socket by lazy {
        this.newHeartBeatSocket()
    }
    override fun heartBeatSocket(): ZMQ.Socket {
//        hb.close()
//        this.zContext.close()
        return hb
    }

    override fun newHeartBeatSocket(): ZMQ.Socket {
        return this.zContext.createSocket(SocketType.REQ).also {
            it.connect(this.channelProvider.heartBeatAddress())
        }
    }

    override fun ioPubSocket(): ZMQ.Socket {
        TODO("Not yet implemented")
    }

    override fun newIOPubSocket(): ZMQ.Socket {
        TODO("Not yet implemented")
    }

    override fun controlSocket(): ZMQ.Socket {
        TODO("Not yet implemented")
    }

    override fun newControlSocket(): ZMQ.Socket {
        TODO("Not yet implemented")
    }
}
