package com.github.xadkile.bicp.message.api.msg.sender.exception

import com.github.xadkile.bicp.message.api.exception.ExceptionInfo
import com.github.xadkile.bicp.message.api.msg.protocol.JPMessage
import org.zeromq.ZMsg

class UnableToQueueMsgException(val exceptionInfo: ExceptionInfo<JPMessage<*, *>>) : Exception(exceptionInfo.toString()) {
    constructor(msg: JPMessage<*, *>) : this(ExceptionInfo("", "", msg))
}

class UnableToQueueZMsgException(val exceptionInfo: ExceptionInfo<ZMsg>) : Exception(exceptionInfo.toString()) {
    constructor(msg: ZMsg) : this(ExceptionInfo("", "", msg))
}
