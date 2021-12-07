package com.github.xadkile.bicp.message.api.msg.sender.exception

import com.github.xadkile.bicp.message.api.exception.ExceptionInfo
import com.github.xadkile.bicp.message.api.msg.protocol.JPMessage

class UnableToSendMsgException(
    val exceptionInfo: ExceptionInfo<JPMessage<*, *>>,
) : Exception(exceptionInfo.toString()) {

    constructor(msg: JPMessage<*, *>) : this(
        ExceptionInfo(
            "", "",
            msg,
        )
    )

    fun getMsg():JPMessage<*,*>{
        return this.exceptionInfo.data
    }
}
