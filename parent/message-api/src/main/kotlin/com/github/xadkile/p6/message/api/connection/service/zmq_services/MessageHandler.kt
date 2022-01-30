package com.github.xadkile.p6.message.api.connection.service.zmq_services

interface MessageHandler {
    fun handleMessage(data:List<ByteArray>)
    val id:String
}
