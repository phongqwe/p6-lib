package com.github.xadkile.p6.message.api.connection.service.zmq_services

import com.github.xadkile.p6.message.api.connection.service.Service
import org.zeromq.SocketType

/**
 * A service that listen to message, could be any type of socket.
 */
interface ZMQSocketListenerService : Service {
    fun addListener(listener: MessageHandler)
    fun removeListener(id:String):Boolean
    fun getListener(id:String): MessageHandler?
    val zmqPort:Int
    val socketType:SocketType
}
