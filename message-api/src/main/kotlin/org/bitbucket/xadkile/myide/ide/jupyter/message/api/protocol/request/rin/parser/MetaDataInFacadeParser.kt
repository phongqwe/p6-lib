package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rin.parser

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rout.OutMetaData

fun interface MetaDataInFacadeParser<M : InMetaData.InFacade> {
    fun parse(input: String): M
}
