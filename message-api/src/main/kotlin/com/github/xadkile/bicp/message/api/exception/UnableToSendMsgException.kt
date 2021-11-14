package com.github.xadkile.bicp.message.api.exception

import com.github.xadkile.bicp.message.api.protocol.message.MsgMetaData
import com.github.xadkile.bicp.message.api.sender.shell.ExecuteRequestInputMessage

class UnableToSendMsgException(
    val msg: ExecuteRequestInputMessage
) : Exception(){
}
