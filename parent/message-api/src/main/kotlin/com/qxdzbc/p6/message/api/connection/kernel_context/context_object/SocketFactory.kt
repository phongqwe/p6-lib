package com.qxdzbc.p6.message.api.connection.kernel_context.context_object

import org.zeromq.ZMQ

/**
 * provide method to create ready-to-use zmq sockets
 */
interface SocketFactory {
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

