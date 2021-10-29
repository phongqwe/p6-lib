package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message

object EmptyMsgContent : OutMsgContent {
    override fun toFacade(): Facade {
        return Facade
    }
    object Facade :
        OutMsgContent.Facade
}
