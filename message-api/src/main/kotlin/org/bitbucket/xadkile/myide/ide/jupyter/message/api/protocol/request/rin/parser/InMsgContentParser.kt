package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rin.parser

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.InMsgContent
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.OutMsgContent

fun interface InMsgContentParser <I:InMsgContent.Facade,O: InMsgContent>{
    fun parse(input:I):O
}
