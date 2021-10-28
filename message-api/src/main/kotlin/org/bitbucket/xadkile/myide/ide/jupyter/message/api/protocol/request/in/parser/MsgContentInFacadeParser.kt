package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.`in`.parser

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgContent

fun interface MsgContentInFacadeParser<O : MsgContent.InFacade> {
    fun parse(input: String): O
}
