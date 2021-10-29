package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rin

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.MessageHeader
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgContentIn
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.session.Session

class RequestIn<META : MetaDataIn, CONTENT : MsgContentIn>(
    private val identities: String,
    private val delimiter: String,
    private val header: MessageHeader,
    private val parentHeader: MessageHeader?,
    private val metadata: META?,
    private val content: CONTENT,
    private val buffer: ByteArray,
    private val key: String,
    private val session: Session,
){
    fun getContent():CONTENT{
        return this.content
    }
}
