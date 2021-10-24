package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.control

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.Content
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MessageEncap


class ShutdownMessageEncap private constructor(val restart: Boolean): MessageEncap, Content, Content.Facade {

    private constructor():this(false)

    companion object{
        val Restart = ShutdownMessageEncap(true)
        val NoRestart = ShutdownMessageEncap(false)
    }

    override fun getMsgType(): String {
        return "shutdown_request"
    }

    override fun getContent(): Content {
        return this
    }

    override fun toFacade(): Content.Facade {
        return this
    }
}
