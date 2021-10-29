package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rin

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.MessageHeader
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rout.OutMetaData
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.InMsgContent
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rin.parser.InMetaData
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.session.Session

class InRequestFacade<META:InMetaData,META_F : InMetaData.InFacade<META>, CONTENT:InMsgContent,CONTENT_F : InMsgContent.Facade<CONTENT>>(
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
    ): Result<InRequest<META, CONTENT>, Exception> {
        val o = binding<InRequest<META, CONTENT>, Exception> {
            val header = this@InRequestFacade.header.toModel().bind()
            val parentHeader = this@InRequestFacade.parentHeader?.toModel()?.bind()
            val k = InRequest<META,CONTENT>(
                identities = this@InRequestFacade.identities,
                delimiter = this@InRequestFacade.delimiter,
                header = header,
                parentHeader = parentHeader,
                metadata = this@InRequestFacade.metadata?.toModel(),
                content = this@InRequestFacade.content.toModel(),
                buffer = this@InRequestFacade.buffer,
                key = this@InRequestFacade.key,
                session = this@InRequestFacade.session
            )
            k
        }
        return o
    }
}
