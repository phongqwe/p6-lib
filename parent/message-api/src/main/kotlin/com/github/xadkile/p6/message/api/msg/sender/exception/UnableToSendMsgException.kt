package com.github.xadkile.p6.message.api.msg.sender.exception

import com.github.xadkile.p6.exception.ExceptionInfo
import com.github.xadkile.p6.message.api.msg.protocol.JPMessage

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
