package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.data_definition.iopub.displaydata

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgContentOut

class IOPubDisplayDataContent(
    val data:Map<String,Any>,
    val metadata:Map<String,Any>,
    val transient:Map<String,Any>
):MsgContentOut {
    override fun toFacade(): MsgContentOut.Facade {
        TODO("Not yet implemented")
    }
}
