package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rin.parser

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.InMsgContent

fun interface InMsgContentFacadeParser<O : InMsgContent.Facade> {
    fun parse(input: String): O
}
