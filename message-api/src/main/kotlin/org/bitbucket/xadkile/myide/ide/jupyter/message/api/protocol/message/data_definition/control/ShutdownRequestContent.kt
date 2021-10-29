package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.data_definition.control

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgContentOut

/**
 * Control.shutdown_request
 */
class ShutdownRequestContent private constructor(val restart: Boolean): MsgContentOut {
    override fun toFacade(): MsgContentOut.Facade {
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
        MsgContentOut.Facade
}

