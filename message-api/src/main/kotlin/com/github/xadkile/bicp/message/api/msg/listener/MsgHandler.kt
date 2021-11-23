package com.github.xadkile.bicp.message.api.msg.listener

import com.github.xadkile.bicp.message.api.protocol.message.JPRawMessage

interface MsgHandler {
    fun handle(msg: JPRawMessage)
    fun id():String
}
