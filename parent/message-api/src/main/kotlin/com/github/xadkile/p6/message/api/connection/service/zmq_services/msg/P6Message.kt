package com.github.xadkile.p6.message.api.connection.service.zmq_services.msg

import com.github.xadkile.message.api.proto.P6MsgPM.*

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
    fun toProto(): P6MessageHeaderProto {
        val eventType = P6EventProto.newBuilder()
            .setCode(this.eventType.code)
            .setName(this.eventType.name)
            .build()
        return P6MessageHeaderProto.newBuilder()
            .setEventType(eventType)
            .setMsgId(msgId)
            .build()
    }
}

fun P6MessageHeaderProto.toModel(): P6MessageHeader {
    return P6MessageHeader(
        msgId = msgId,
        eventType = eventType.toModel(),
    )
}

fun P6EventProto.toModel():P6Event{
    return P6Event(
        code = code,
        name = name,
    )
}

data class P6MessageContent(val data: String)

data class P6Message(val header: P6MessageHeader, val content: P6MessageContent) {
    fun toProto(): P6MessageProto {
        return P6MessageProto.newBuilder()
            .setHeader(header.toProto())
            .setData(content.data)
            .build()
    }
}

fun P6MessageProto.toModel(): P6Message {
    return P6Message(
        header = header.toModel(),
        content = P6MessageContent(
            data = data
        )
    )
}
