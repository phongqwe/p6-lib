package com.github.xadkile.p6.message.api.connection.service.zmq_services

import com.github.xadkile.p6.message.api.connection.service.zmq_services.msg.P6Message
import java.util.*

object P6MsgHandlers {
    fun makeHandler(callback:(msg:P6Message)->Unit):P6MessageHandler{
        return object : P6MessageHandler {
            override fun handleMessage(msg: P6Message) {
                callback(msg)
            }
            override val id: String = UUID.randomUUID().toString()
        }
    }
}
