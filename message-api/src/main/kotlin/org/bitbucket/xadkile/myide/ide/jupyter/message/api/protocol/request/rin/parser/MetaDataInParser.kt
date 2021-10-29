package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rin.parser

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rout.OutMetaData

fun interface MetaDataInParser<I: InMetaData.InFacade,O: InMetaData> {
    fun parse(input:I):O
}
