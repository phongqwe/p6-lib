package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.`in`.parser

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.MetaData

fun interface MetaDataInParser<I:MetaData.InFacade,O:MetaData> {
    fun parse(input:I):O
}
