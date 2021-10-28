package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgContent
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.session.Session

class InRequest<META : MetaData, CONTENT : MsgContent>(
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
