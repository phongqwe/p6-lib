package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rout


interface OutMetaData {
    interface OutFacade
    fun toFacade(): OutFacade
}

