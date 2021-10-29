package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rin.parser

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.InMsgContent

fun interface InMsgContentFacadeParser<CONTENT:InMsgContent,CONTENT_F : InMsgContent.Facade<CONTENT>> {
    fun parse(input: String): CONTENT_F
}
