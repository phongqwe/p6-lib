package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.control

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.OutMsgContent

/**
 * Control.shutdown_request
 */
class ShutdownRequestContent private constructor(val restart: Boolean): OutMsgContent {
    override fun toFacade(): OutMsgContent.Facade {
        return Facade(this.restart)
    }

    companion object {
        val noRestart = ShutdownRequestContent(false)
        val restart = ShutdownRequestContent(true)
    }

    /**
     * must create separated class for facade, otherwise gson will change attribute names to incorrect one
     */
    class Facade(val restart: Boolean):
        OutMsgContent.Facade
}

