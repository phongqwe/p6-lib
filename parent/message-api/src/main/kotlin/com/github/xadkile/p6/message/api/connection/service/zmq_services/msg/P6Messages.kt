package com.github.xadkile.p6.message.api.connection.service.zmq_services.msg

import com.google.protobuf.ByteString
import java.util.*

object P6Messages {
    fun p6Message(event: P6Event, data: ByteString): P6Message {
        return P6Message(
            header = P6MessageHeader(UUID.randomUUID().toString(), event),
            data = data
        )
    }
    fun p6Response(event: P6Event, data:ByteString, status: Status):P6Response{
        return P6Response(
            header =P6MessageHeader(UUID.randomUUID().toString(), event),
            status = status,
            data = data
        )
    }
}
