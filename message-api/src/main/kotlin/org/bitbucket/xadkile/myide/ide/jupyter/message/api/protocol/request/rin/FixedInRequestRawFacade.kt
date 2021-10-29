package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rin

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.google.gson.GsonBuilder
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgContentIn
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.session.Session

/**
 * For reading raw data from zmq sockets
 * Fixed = fixed output model
 */
class FixedInRequestRawFacade<
        META_F : MetaDataIn.InFacade<META>,
        CONTENT_F : MsgContentIn.Facade<CONTENT>,
        META : MetaDataIn,
        CONTENT : MsgContentIn,
        >(
    val rawFacade: RequestRawFacadeIn,
    val metaDataFacadeParser: MetaDataInFacadeParser<META, META_F>,
    val contentInFacadeParser: InMsgContentFacadeParser<CONTENT, CONTENT_F>,
) : CanVerifyHmac {
    companion object {

        inline fun <
                reified META_F : MetaDataIn.InFacade<META>,
                reified CONTENT_F : MsgContentIn.Facade<CONTENT>,
                META : MetaDataIn,
                CONTENT : MsgContentIn,
                >
                defaultCreate(rawFacade: RequestRawFacadeIn):
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
                META_F : MetaDataIn.InFacade<META>,
                CONTENT_F : MsgContentIn.Facade<CONTENT>,
                META : MetaDataIn,
                CONTENT : MsgContentIn,
                >
                fromRecvPayload(
            payload: List<ByteArray>,
            metaDataFacadeParser: MetaDataInFacadeParser<META, META_F>,
            contentInFacadeParser: InMsgContentFacadeParser<CONTENT, CONTENT_F>,
        ): Result<FixedInRequestRawFacade<META_F, CONTENT_F, META, CONTENT>, Exception> {

            val rf: Result<RequestRawFacadeIn, Exception> = RequestRawFacadeIn.fromRecvPayload(payload)
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
                reified META_F : MetaDataIn.InFacade<META>,
                reified CONTENT_F : MsgContentIn.Facade<CONTENT>,
                META : MetaDataIn,
                CONTENT : MsgContentIn,
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
    ): Result<RequestIn<META, CONTENT>, Exception> {
        return this.rawFacade.toModel(
            this.metaDataFacadeParser, this.contentInFacadeParser, session
        )
    }

    fun toFacade(
        session: Session,
    ): Result<RequestFacadeIn<META, META_F, CONTENT, CONTENT_F>, Exception> {
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
