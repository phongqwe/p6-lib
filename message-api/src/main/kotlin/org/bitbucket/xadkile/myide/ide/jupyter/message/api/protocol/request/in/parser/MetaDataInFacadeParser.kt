package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.`in`.parser

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.MetaData

fun interface MetaDataInFacadeParser<M : MetaData.InFacade> {
    fun parse(input: String): M
}
