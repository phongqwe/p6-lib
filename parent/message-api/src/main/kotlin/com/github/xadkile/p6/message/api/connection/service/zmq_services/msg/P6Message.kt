package com.github.xadkile.p6.message.api.connection.service.zmq_services.msg

enum class P6MsgType {
    cell_value_update,
    worksheet_update
}

data class P6MessageHeader(val msgId: String,val msgType: P6MsgType)
data class P6MessageContent(val data: String)
data class P6Message(val header: P6MessageHeader, val content: P6MessageContent)
