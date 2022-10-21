package com.qxdzbc.p6.message.api.connection.service.zmq_services

import com.qxdzbc.p6.message.api.connection.service.zmq_services.msg.P6Message
import com.qxdzbc.p6.message.api.connection.service.zmq_services.msg.P6Response

interface P6Handler<T> {
    val id:String
    fun handleMessage(t:T)
}
