package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message

object EmptyMsgContent : MsgContentOut {
    override fun toFacade(): Facade {
        return Facade
    }
    object Facade :
        MsgContentOut.Facade
}
