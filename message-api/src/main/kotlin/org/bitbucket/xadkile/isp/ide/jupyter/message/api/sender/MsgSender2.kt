package org.bitbucket.xadkile.isp.ide.jupyter.message.api.sender

import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message.JPMessage
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message.MsgContent
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message.MsgMetaData
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message.MsgType

interface MsgSender2<M: MsgMetaData,C: MsgContent,O> {
    fun send(message:JPMessage<M,C>):O
}
