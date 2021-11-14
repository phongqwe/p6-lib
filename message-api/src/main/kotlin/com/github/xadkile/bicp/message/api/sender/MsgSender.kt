package com.github.xadkile.bicp.message.api.sender

import com.github.xadkile.bicp.message.api.protocol.message.JPMessage
import com.github.xadkile.bicp.message.api.protocol.message.MsgContent
import com.github.xadkile.bicp.message.api.protocol.message.MsgMetaData

/**
 * Send a JPMessage
 */
interface MsgSender<I:JPMessage<*,*>,O> {
    fun send(message:I):O
}
