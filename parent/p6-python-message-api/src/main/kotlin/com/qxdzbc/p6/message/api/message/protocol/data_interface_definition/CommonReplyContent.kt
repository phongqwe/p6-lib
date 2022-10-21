package com.qxdzbc.p6.message.api.message.protocol.data_interface_definition

import com.qxdzbc.p6.message.api.message.protocol.MsgStatus


sealed class CommonReplyContent(
    val status: MsgStatus,
    override val traceback: List<String>,
    val ename:String,
    val evalue:String
):WithTraceBack

