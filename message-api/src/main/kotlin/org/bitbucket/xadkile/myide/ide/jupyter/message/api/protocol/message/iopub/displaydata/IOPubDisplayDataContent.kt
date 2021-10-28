package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.iopub.displaydata

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgContent

class IOPubDisplayDataContent(
    val data:Map<String,Any>,
    val metadata:Map<String,Any>,
    val transient:Map<String,Any>
):MsgContent {
    override fun toFacade(): MsgContent.OutFacade {
        TODO("Not yet implemented")
    }
}
