package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rin

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.google.gson.GsonBuilder
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.InMsgContent
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rin.parser.InMetaData
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rin.parser.InMsgContentFacadeParser
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rin.parser.MetaDataInFacadeParser
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.session.Session

/**
 * For reading raw data from zmq sockets
 * Fixed = fixed output model
 */
class FixedInRequestRawFacade<
        META_F : InMetaData.InFacade<META>,
        CONTENT_F : InMsgContent.Facade<CONTENT>,
        META : InMetaData,
        CONTENT : InMsgContent,
        >(
    val rawFacade: InRequestRawFacade,
    val metaDataFacadeParser: MetaDataInFacadeParser<META, META_F>,
    val contentInFacadeParser: InMsgContentFacadeParser<CONTENT, CONTENT_F>,
) : CanVerifyHmac {
    companion object {

        inline fun <
                reified META_F : InMetaData.InFacade<META>,
                reified CONTENT_F : InMsgContent.Facade<CONTENT>,
                META : InMetaData,
                CONTENT : InMsgContent,
                >
                defaultCreate(rawFacade: InRequestRawFacade):
                FixedInRequestRawFacade<META_F, CONTENT_F, META, CONTENT> {
            val mp = MetaDataInFacadeParser.jsonParser<META, META_F>()
            val cp = InMsgContentFacadeParser.jsonParser<CONTENT, CONTENT_F>()
            return FixedInRequestRawFacade(rawFacade, mp, cp)
        }

        /**
         * Parse a list of byte array into a [FixedInRequestRawFacade].
         *
         * Detect the delimiter and use it as a pivot point to locate other elements
         */
        fun <
                META_F : InMetaData.InFacade<META>,
                CONTENT_F : InMsgContent.Facade<CONTENT>,
                META : InMetaData,
                CONTENT : InMsgContent,
                >
                fromRecvPayload(
            payload: List<ByteArray>,
            metaDataFacadeParser: MetaDataInFacadeParser<META, META_F>,
            contentInFacadeParser: InMsgContentFacadeParser<CONTENT, CONTENT_F>,
        ): Result<FixedInRequestRawFacade<META_F, CONTENT_F, META, CONTENT>, Exception> {

            val rf: Result<InRequestRawFacade, Exception> = InRequestRawFacade.fromRecvPayload(payload)
            val rt: Result<FixedInRequestRawFacade<META_F, CONTENT_F, META, CONTENT>, Exception> = rf.map {
                FixedInRequestRawFacade(
                    rawFacade = it,
                    metaDataFacadeParser = metaDataFacadeParser,
                    contentInFacadeParser = contentInFacadeParser
                )
            }

            return rt
        }

        inline fun <
                reified META_F : InMetaData.InFacade<META>,
                reified CONTENT_F : InMsgContent.Facade<CONTENT>,
                META : InMetaData,
                CONTENT : InMsgContent,
                >
                fromRecvPayload(
            payload: List<ByteArray>,
        ): Result<FixedInRequestRawFacade<META_F, CONTENT_F, META, CONTENT>, Exception> {

            val rt: Result<FixedInRequestRawFacade<META_F, CONTENT_F, META, CONTENT>, Exception> =
                fromRecvPayload(payload,
                    metaDataFacadeParser = MetaDataInFacadeParser.jsonParser(),
                    contentInFacadeParser = InMsgContentFacadeParser.jsonParser()
                )

            return rt
        }
    }

    fun toModel(
        session: Session,
    ): Result<InRequest<META, CONTENT>, Exception> {
        return this.rawFacade.toModel(
            this.metaDataFacadeParser, this.contentInFacadeParser, session
        )
    }

    fun toFacade(
        session: Session,
    ): Result<InRequestFacade<META, META_F, CONTENT, CONTENT_F>, Exception> {
        return this.rawFacade.toFacade(
            this.metaDataFacadeParser,
            this.contentInFacadeParser,
            session
        )
    }

    override fun verifyHmac(key: ByteArray): Boolean {
        return this.rawFacade.verifyHmac(key)
    }

    override fun toString(): String {
        return GsonBuilder().setPrettyPrinting().create().toJson(this.rawFacade)
    }
}
