package com.github.xadkile.p6.message.api.connection.service.zmq_services

import com.github.xadkile.p6.message.api.connection.service.Service
import org.zeromq.SocketType

/**
 * A service that listen to message, could be any type of socket.
 */
interface ZMQSocketListenerService : Service {
    fun addHandler(handler: MessageHandler)
    fun removeHandler(id:String):Boolean
    fun getHandler(id:String): MessageHandler?
    val zmqPort:Int
    val socketType:SocketType
}
