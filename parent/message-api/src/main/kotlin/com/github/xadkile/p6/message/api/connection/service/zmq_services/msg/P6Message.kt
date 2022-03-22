package com.github.xadkile.p6.message.api.connection.service.zmq_services.msg

import com.github.xadkile.message.api.proto.P6MsgPM.*

enum class P6EventType {
    cell_value_update,
    worksheet_update,
    worksheet_rename_ok,
    unknown,
    ;

    companion object {
        fun fromStr(str:String):P6EventType{
            try{
                return P6EventType.valueOf(str)
            }catch (e:Exception){
                return unknown
            }
        }
    }
}

data class P6MessageHeader(val msgId: String,val eventType: P6EventType){
    fun toProto(): P6MessageHeaderProto {
        return P6MessageHeaderProto.newBuilder()
            .setEventType(eventType.name)
            .setMsgId(msgId)
            .build()
    }
}

fun P6MessageHeaderProto.toModel():P6MessageHeader{
    return P6MessageHeader(
        msgId = msgId,
        eventType = P6EventType.fromStr(eventType)
    )
}

data class P6MessageContent(val data: String)

data class P6Message(val header: P6MessageHeader, val content: P6MessageContent){
    fun toProto():P6MessageProto{
        return P6MessageProto.newBuilder()
            .setHeader(header.toProto())
            .setData(content.data)
            .build()
    }
}

fun P6MessageProto.toModel():P6Message{
    return P6Message(
        header= header.toModel(),
        content =P6MessageContent(
            data = data
        )
    )
}
