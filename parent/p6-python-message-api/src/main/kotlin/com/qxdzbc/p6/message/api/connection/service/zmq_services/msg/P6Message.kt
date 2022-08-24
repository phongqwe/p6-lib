package com.qxdzbc.p6.message.api.connection.service.zmq_services.msg

import com.qxdzbc.p6.proto.P6MsgProtos
import com.google.protobuf.ByteString

data class P6Event(val code: String, val name: String){
    override fun equals(other: Any?): Boolean {
        if(other != null && other is P6Event){
            return this.code == other.code
        }
        return false
    }

    override fun hashCode(): Int {
        return this.code.hashCode()
    }
}

data class P6MessageHeader(val msgId: String, val eventType: P6Event) {
    fun toProto(): P6MsgProtos.P6MessageHeaderProto {
        val eventType = P6MsgProtos.P6EventProto.newBuilder()
            .setCode(this.eventType.code)
            .setName(this.eventType.name)
            .build()
        return P6MsgProtos.P6MessageHeaderProto.newBuilder()
            .setEventType(eventType)
            .setMsgId(msgId)
            .build()
    }
}

fun P6MsgProtos.P6MessageHeaderProto.toModel(): P6MessageHeader {
    return P6MessageHeader(
        msgId = msgId,
        eventType = eventType.toModel(),
    )
}

fun P6MsgProtos.P6EventProto.toModel():P6Event{
    return P6Event(
        code = code,
        name = name,
    )
}


data class P6Message(val header: P6MessageHeader, val data: ByteString) {
    fun toProto(): P6MsgProtos.P6MessageProto {
        return P6MsgProtos.P6MessageProto.newBuilder()
            .setHeader(header.toProto())
            .setData(data)
            .build()
    }

    val event = header.eventType
    val id = header.msgId
}

fun P6MsgProtos.P6MessageProto.toModel(): P6Message {
    return P6Message(
        header = header.toModel(),
        data = data
    )
}
