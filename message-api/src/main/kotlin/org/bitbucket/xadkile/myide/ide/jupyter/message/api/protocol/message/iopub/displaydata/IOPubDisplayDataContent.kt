package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.iopub.displaydata

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.OutMsgContent

class IOPubDisplayDataContent(
    val data:Map<String,Any>,
    val metadata:Map<String,Any>,
    val transient:Map<String,Any>
):OutMsgContent {
    override fun toFacade(): OutMsgContent.Facade {
        TODO("Not yet implemented")
    }
}
