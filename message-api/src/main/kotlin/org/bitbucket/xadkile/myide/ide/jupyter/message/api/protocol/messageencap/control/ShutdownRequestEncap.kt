package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.messageencap.control

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.messageencap.MsgContent
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.messageencap.MsgEncap
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.messageencap.MsgType

/**
 * @deprecated dont use, kept just in case
 */
class ShutdownRequestEncap private constructor(val restart: Boolean): MsgEncap, MsgContent, MsgContent.Facade {

    private constructor():this(false)

    companion object{
        val Restart = ShutdownRequestEncap(true)
        val NoRestart = ShutdownRequestEncap(false)
    }

    override fun getMsgType(): MsgType {
        return MsgType.Control.shutdown_request
    }

    override fun getContent(): MsgContent {
        return this
    }

    override fun toFacade(): MsgContent.Facade {
        return this
    }
}

