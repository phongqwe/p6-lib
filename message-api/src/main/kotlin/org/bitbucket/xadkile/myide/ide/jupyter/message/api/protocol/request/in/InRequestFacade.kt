package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgContent
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.`in`.parser.MetaDataInParser
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.`in`.parser.MsgContentInParser
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.session.Session

class InRequestFacade<META_F : MetaData.InFacade, CONTENT_F : MsgContent.InFacade>(
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
    fun <META : MetaData, CONTENT : MsgContent> toModel(
        metaDataInParser: MetaDataInParser<META_F,META>,
        contentInParser: MsgContentInParser<CONTENT_F,CONTENT>,
    ): Result<InRequest<META, CONTENT>, Exception> {
        val o = binding<InRequest<META, CONTENT>, Exception> {
            val header = this@InRequestFacade.header.toModel().bind()
            val parentHeader = this@InRequestFacade.parentHeader?.toModel()?.bind()
            val k = InRequest(
                identities = this@InRequestFacade.identities,
                delimiter = this@InRequestFacade.delimiter,
                header = header,
                parentHeader = parentHeader,
                metadata = this@InRequestFacade.metadata?.let { metaDataInParser.parse(it) },
                content = contentInParser.parse(this@InRequestFacade.content),
                buffer = this@InRequestFacade.buffer,
                key = this@InRequestFacade.key,
                session = this@InRequestFacade.session
            )
            k
        }
        return o
    }

//    fun <META : MetaData, CONTENT : MsgContent> toModel(
//        metaDataInParser: (META_F)->META,
//        contentInParser: (CONTENT_F)->CONTENT,
//    ): Result<InRequest<META, CONTENT>, Exception> {
//        val o = binding<InRequest<META, CONTENT>, Exception> {
//            val header = this@InRequestFacade.header.toModel().bind()
//            val parentHeader = this@InRequestFacade.parentHeader?.toModel()?.bind()
//            val k = InRequest(
//                identities = this@InRequestFacade.identities,
//                delimiter = this@InRequestFacade.delimiter,
//                header = header,
//                parentHeader = parentHeader,
//                metadata = this@InRequestFacade.metadata?.let { metaDataInParser(it) },
//                content = contentInParser(this@InRequestFacade.content),
//                buffer = this@InRequestFacade.buffer,
//                key = this@InRequestFacade.key,
//                session = this@InRequestFacade.session
//            )
//            k
//        }
//        return o
//    }
}
