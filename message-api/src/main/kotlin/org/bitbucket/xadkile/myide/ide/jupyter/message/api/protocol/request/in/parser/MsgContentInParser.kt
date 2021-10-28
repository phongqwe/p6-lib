package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.`in`.parser

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgContent

fun interface MsgContentInParser <I:MsgContent.InFacade,O: MsgContent>{
    fun parse(input:I):O
}
