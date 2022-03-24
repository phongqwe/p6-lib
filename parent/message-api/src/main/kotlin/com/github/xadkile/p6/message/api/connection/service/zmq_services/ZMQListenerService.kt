package com.github.xadkile.p6.message.api.connection.service.zmq_services

import com.github.xadkile.p6.message.api.connection.service.Service
import org.slf4j.MarkerFactory
import org.zeromq.SocketType

/**
 * A service that listen to message, could be any type of socket.
 */
interface ZMQListenerService : Service,P6MsgHandlerContainer {
    companion object{
        val marker = MarkerFactory.getMarker(ZMQListenerService::class.java.canonicalName)
    }
    val zmqPort:Int
    val socketType:SocketType
}
