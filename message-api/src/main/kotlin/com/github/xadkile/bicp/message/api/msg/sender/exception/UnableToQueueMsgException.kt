package com.github.xadkile.bicp.message.api.msg.sender.exception

import com.github.xadkile.bicp.message.api.msg.protocol.message.JPMessage
import org.zeromq.ZMsg

class UnableToQueueMsgException(
    val msg: JPMessage<*, *>
):Exception()

class UnableToQueueZMsgException(
    val msg: ZMsg
):Exception()
