package com.github.xadkile.p6.message.api.connection.service.zmq_services

import com.github.xadkile.p6.message.api.connection.service.Service
import com.github.xadkile.p6.message.api.connection.service.zmq_services.msg.P6MsgType
import org.zeromq.SocketType

/**
 * A service that listen to message, could be any type of socket.
 */
interface ZMQSocketListenerService : Service,P6MsgHandlerContainer {
    val zmqPort:Int
    val socketType:SocketType
}
