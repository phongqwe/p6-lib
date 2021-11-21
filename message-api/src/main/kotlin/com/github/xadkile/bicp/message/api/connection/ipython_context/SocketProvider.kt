package com.github.xadkile.bicp.message.api.connection.ipython_context

import org.zeromq.ZMQ

/**
 * provide cached and ready-to-use sockets that can be reused, and methods to create new and ready-to-use sockets.
 * The question is, should the state of the cached socket be managed by this provider, or leave it to the context?
 * If my apps run for long periods, with multiple interruptions of IPython in between, then there will be a lot of un-closed socket, leaking in the memory because:
 *  - The socket won't be reachable as the old SocketProvider is destroyed after each restart of IPython context => lead to the destruction of the reference to old cached Socket.
 *  - However, a single ZContext is kept around for the entirety of the app even when IPython context stop, the same ZContext persists. This ZContext still hold reference of the old socket somewhere in its memory
 *  => inaccessible but still valid reference => leak
 *
 *  I have two choice to prevent this:
 *  - manage the socket life cycle by delegate this responsiblity to socket providers and tie the life cycle of the cached socket to the life cycle of the socket provider
 *          + socket provider become stateful
 *  - OR, avoid single ZContext and create new ZContext each time IPython Context restart?
 *          +
 *
 *  Important: Whatever I do, I must maintain 1 single instance of ZContext. So that ZMQ resources (socket, poller,...) can be cleaned up properly.
 *
 *  For now, ZContext is injected into IPython context. This means in can be used for other purposes as well. If I tied this ZContext instance lifecycle to IPython, then everything that use this ZContext instance will depends on IPython context. This is very pervasive.
 */
interface SocketProvider {
    fun shellSocket():ZMQ.Socket
    fun newShellSocket():ZMQ.Socket
    fun heartBeatSocket():ZMQ.Socket
    fun newHeartBeatSocket():ZMQ.Socket
    fun ioPubSocket(): ZMQ.Socket
    fun newIOPubSocket(): ZMQ.Socket
    fun controlSocket(): ZMQ.Socket
    fun newControlSocket(): ZMQ.Socket
//    fun closeCachedSocket()
}

