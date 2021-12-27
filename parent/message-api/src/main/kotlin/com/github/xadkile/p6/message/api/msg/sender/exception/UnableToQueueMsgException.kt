package com.github.xadkile.p6.message.api.msg.sender.exception

import com.github.xadkile.p6.exception.ExceptionInfo
import com.github.xadkile.p6.message.api.msg.protocol.JPMessage
import org.zeromq.ZMsg

class UnableToQueueZMsgException(val exceptionInfo: ExceptionInfo<ZMsg>) : Exception(exceptionInfo.toString()) {
    constructor(msg: ZMsg) : this(ExceptionInfo("", "", msg))
}
