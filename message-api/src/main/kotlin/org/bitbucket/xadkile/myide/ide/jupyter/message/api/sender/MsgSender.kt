package org.bitbucket.xadkile.myide.ide.jupyter.message.api.sender

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.messageencap.MsgContent
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.messageencap.MsgEncap
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.messageencap.MsgType

interface MsgSender {
    fun send(msgType: MsgType, msgContent:MsgContent)
}
