package com.github.xadkile.bicp.message.api.sender

import com.github.xadkile.bicp.message.api.protocol.message.MsgContent
import com.github.xadkile.bicp.message.api.protocol.message.MsgType


interface MsgSender<I: MsgContent,O> {
    /**
     * send a [msgContent] of type [msgType] to somewhere
     * TODO reconsider this. Should this only accept Content or should this accept the whole message
     */
    fun send(msgType: MsgType, msgContent:I):O
}
