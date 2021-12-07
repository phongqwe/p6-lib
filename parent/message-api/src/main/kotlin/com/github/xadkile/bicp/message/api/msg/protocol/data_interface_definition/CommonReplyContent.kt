package com.github.xadkile.bicp.message.api.msg.protocol.data_interface_definition

import com.github.xadkile.bicp.message.api.msg.protocol.MsgStatus


sealed class CommonReplyContent(
    val status: MsgStatus,
    val traceback: List<String>,
    val ename:String,
    val evalue:String
)

