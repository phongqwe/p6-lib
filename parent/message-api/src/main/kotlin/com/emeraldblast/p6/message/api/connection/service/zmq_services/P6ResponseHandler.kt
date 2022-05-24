package com.emeraldblast.p6.message.api.connection.service.zmq_services

import com.emeraldblast.p6.message.api.connection.service.iopub.UUIDMsgHandler
import com.emeraldblast.p6.message.api.connection.service.zmq_services.msg.P6Message
import com.emeraldblast.p6.message.api.connection.service.zmq_services.msg.P6Response
import java.util.UUID

interface P6ResponseHandler :P6Handler<P6Response> {
}

abstract class BaseP6ResponseHandler:P6ResponseHandler{
    override val id: String = UUID.randomUUID().toString()
}

