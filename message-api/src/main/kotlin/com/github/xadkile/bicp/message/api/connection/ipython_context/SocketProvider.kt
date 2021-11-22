package com.github.xadkile.bicp.message.api.connection.ipython_context

import org.zeromq.ZMQ

/**
 * provide cached and ready-to-use sockets that can be reused, and methods to create new and ready-to-use sockets.
 */
interface SocketProvider {
    fun shellSocket():ZMQ.Socket
    fun heartBeatSocket():ZMQ.Socket
    fun ioPubSocket(): ZMQ.Socket
    fun controlSocket(): ZMQ.Socket
}

