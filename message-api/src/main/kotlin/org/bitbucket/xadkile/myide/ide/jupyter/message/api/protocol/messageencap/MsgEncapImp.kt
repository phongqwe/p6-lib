package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.messageencap


/**
 * basic implementation of [MsgEncap]
 * @deprecated dont use, kept just in case
 */
class MsgEncapImp(private val msgType: MsgType, private val content:MsgContent) : MsgEncap{
    override fun getMsgType(): MsgType {
        return this.msgType
    }

    override fun getContent(): MsgContent {
        return this.content
    }
}
