package org.bitbucket.xadkile.myide.ide.jupyter.message.api.sender

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgContent
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgType

interface MsgSender {
    fun send(msgType: MsgType, msgContent:MsgContent)
}
