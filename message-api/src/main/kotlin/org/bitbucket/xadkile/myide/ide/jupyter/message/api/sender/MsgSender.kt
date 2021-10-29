package org.bitbucket.xadkile.myide.ide.jupyter.message.api.sender

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.OutMsgContent
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgType

interface MsgSender<I:OutMsgContent,O> {
    /**
     * send a [msgContent] of type [msgType] to somewhere
     */
    fun send(msgType: MsgType, msgContent:I):O
}
