package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rout.OutMetaData

object EmptyOutMetaData : OutMetaData {
    override fun toFacade(): Facade{
        return Facade
    }
    object Facade : OutMetaData.OutFacade
}
