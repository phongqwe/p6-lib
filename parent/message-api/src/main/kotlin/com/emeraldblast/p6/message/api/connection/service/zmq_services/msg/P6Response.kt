
package com.emeraldblast.p6.message.api.connection.service.zmq_services.msg

import com.emeraldblast.p6.proto.P6MsgProtos
import com.google.protobuf.ByteString


enum class Status{
    INVALID,OK,ERROR;
    companion object{
        fun fromProto(proto: P6MsgProtos.P6ResponseProto.Status):Status{
            return when(proto){
                P6MsgProtos.P6ResponseProto.Status.OK -> OK
                P6MsgProtos.P6ResponseProto.Status.ERROR -> ERROR
                else -> INVALID
            }
        }
    }
}

data class P6Response(
    val header:P6MessageHeader,
    val status:Status,
    val data: ByteString
)

fun P6MsgProtos.P6ResponseProto.toModel():P6Response{
    return P6Response(
        header = header.toModel(),
        status =  Status.fromProto(status),
        data =  data
    )
}

