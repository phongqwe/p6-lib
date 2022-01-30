package com.github.xadkile.p6.message.api.connection.service.zmq_services

import com.github.xadkile.p6.message.api.connection.service.Service
import org.zeromq.SocketType

/**
 * A service that hosts a SUB socket
 */
interface ZMQSocketListenerService : Service {
    fun addListener(listener: MessageHandler)
    fun removeListener(id:String):Boolean
    fun getListener(id:String): MessageHandler?
    val zmqPort:Int
    val socketType:SocketType
}
