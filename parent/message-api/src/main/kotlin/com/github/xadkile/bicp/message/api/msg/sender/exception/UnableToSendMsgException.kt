package com.github.xadkile.bicp.message.api.msg.sender.exception

import com.github.xadkile.bicp.message.api.exception.ExceptionWithInfo
import com.github.xadkile.bicp.message.api.msg.protocol.JPMessage

class UnableToSendMsgException(
    val msg: JPMessage<*, *>
) : ExceptionWithInfo("","",msg)
