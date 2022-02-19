package com.github.xadkile.p6.message.api.connection.service.zmq_services.msg

enum class P6MsgType {
    cell_value_update
}

class P6MessageHeader(val msgId: String,val msgType: P6MsgType)
class P6MessageContent(val data: String)
class P6Message(val header: P6MessageHeader, val content: P6MessageContent)
