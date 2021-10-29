package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rin.parser

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rout.OutMetaData

fun interface MetaDataInFacadeParser<META:InMetaData,META_F : InMetaData.InFacade<META>> {
    fun parse(input: String): META_F
}
