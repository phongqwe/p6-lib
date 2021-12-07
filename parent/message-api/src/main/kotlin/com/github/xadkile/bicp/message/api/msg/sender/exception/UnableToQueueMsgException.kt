package com.github.xadkile.bicp.message.api.msg.sender.exception

import com.github.xadkile.bicp.message.api.exception.ExceptionInfo
import com.github.xadkile.bicp.message.api.exception.ExceptionWithInfo
import com.github.xadkile.bicp.message.api.msg.protocol.JPMessage
import org.zeromq.ZMsg

class UnableToQueueMsgException(exceptionInfo: ExceptionInfo) : ExceptionWithInfo(exceptionInfo) {
    constructor(msg: JPMessage<*, *>) : this(ExceptionInfo("", "", msg))
}

class UnableToQueueZMsgException(exceptionInfo: ExceptionInfo) : ExceptionWithInfo(exceptionInfo) {
    constructor(msg: ZMsg) : this(ExceptionInfo("", "", msg))
}
