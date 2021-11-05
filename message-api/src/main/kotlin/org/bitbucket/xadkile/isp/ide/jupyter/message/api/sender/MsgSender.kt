package org.bitbucket.xadkile.isp.ide.jupyter.message.api.sender

import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message.MsgContent
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message.MsgType


interface MsgSender<I: MsgContent,O> {
    /**
     * send a [msgContent] of type [msgType] to somewhere
     * TODO reconsider this. Should this only accept Content or should this accept the whole message
     */
    fun send(msgType: MsgType, msgContent:I):O
}
