package com.github.xadkile.p6.message.api.connection.service.zmq_services

import com.github.xadkile.p6.message.api.connection.service.zmq_services.msg.P6Message

interface P6MessageHandler {
    val id:String
    fun handleMessage(msg:P6Message)
    fun handleError(msg:P6Message)
}
