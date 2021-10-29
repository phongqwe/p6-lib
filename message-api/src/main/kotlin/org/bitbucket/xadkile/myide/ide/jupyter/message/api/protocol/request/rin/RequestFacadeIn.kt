package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rin

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.MessageHeader
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgContentIn
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.session.Session

class RequestFacadeIn<META: MetaDataIn,META_F : MetaDataIn.InFacade<META>, CONTENT:MsgContentIn,CONTENT_F : MsgContentIn.Facade<CONTENT>>(
    private val identities: String,
    private val delimiter: String,
    private val header: MessageHeader.Facade,
    private val parentHeader: MessageHeader.Facade?,
    private val metadata: META_F?,
    private val content: CONTENT_F,
    private val buffer: ByteArray,
    private val key: String,
    private val session: Session,
) {
    fun toModel(
//        metaDataInParser: MetaDataInParser<META_F,META>,
//        contentInParser: InMsgContentParser<CONTENT_F,CONTENT>,
    ): Result<RequestIn<META, CONTENT>, Exception> {
        val o = binding<RequestIn<META, CONTENT>, Exception> {
            val header = this@RequestFacadeIn.header.toModel().bind()
            val parentHeader = this@RequestFacadeIn.parentHeader?.toModel()?.bind()
            val k = RequestIn<META,CONTENT>(
                identities = this@RequestFacadeIn.identities,
                delimiter = this@RequestFacadeIn.delimiter,
                header = header,
                parentHeader = parentHeader,
                metadata = this@RequestFacadeIn.metadata?.toModel(),
                content = this@RequestFacadeIn.content.toModel(),
                buffer = this@RequestFacadeIn.buffer,
                key = this@RequestFacadeIn.key,
                session = this@RequestFacadeIn.session
            )
            k
        }
        return o
    }
}
