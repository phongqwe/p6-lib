package com.github.xadkile.bicp.message.api.connection.ipython_context

import org.zeromq.ZMQ

/**
 * provide cached and ready-to-use sockets that can be reused, and methods to create new and ready-to-use sockets.
 */
interface SocketProvider {
    /**
     * create a new shell socket
     */
    fun shellSocket():ZMQ.Socket
    /**
     * create a new heart beat socket
     */
    fun heartBeatSocket():ZMQ.Socket
    /**
     * create a new iopub socket
     */
    fun ioPubSocket(): ZMQ.Socket
    /**
     * create a new control socket
     */
    fun controlSocket(): ZMQ.Socket
}

