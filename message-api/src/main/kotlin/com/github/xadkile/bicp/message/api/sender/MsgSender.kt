package com.github.xadkile.bicp.message.api.sender

import com.github.xadkile.bicp.message.api.protocol.message.JPMessage
import com.github.xadkile.bicp.message.api.protocol.message.MsgContent
import com.github.xadkile.bicp.message.api.protocol.message.MsgMetaData
import com.github.xadkile.bicp.message.api.protocol.message.MsgType

/**
 * Sender must not be initated directly, must be produced by [SenderProvider]
 */
interface MsgSender<M: MsgMetaData,C: MsgContent,O> {
    fun send(message:JPMessage<M,C>):O
}
