package com.github.xadkile.p6.message.api.connection.service.zmq_services

import com.github.xadkile.p6.message.api.connection.service.Service
import com.github.xadkile.p6.message.api.connection.service.zmq_services.msg.P6MsgType
import org.zeromq.SocketType

/**
 * A service that listen to message, could be any type of socket.
 */
interface ZMQSocketListenerService : Service {
    fun addHandler(msgType:P6MsgType,handler: P6MessageHandler)

    /**
     * return the removed handlers
     */
    fun removeHandlerForMsgType(msgType: P6MsgType):List<P6MessageHandler>

    /**
     * return the removed handler
     */
    fun removeHandler(msgType: P6MsgType, id:String):P6MessageHandler?
    /**
     * return the removed handler
     */
    fun removeHandler(id:String):P6MessageHandler?
    fun getHandler(id:String): P6MessageHandler?
    val zmqPort:Int
    val socketType:SocketType
}
