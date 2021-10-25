package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.messageencap

/**
 * @deprecated dont use, kept just in case
 */
interface MsgEncap {

    fun getMsgType():MsgType
    fun getContent(): MsgContent
}
