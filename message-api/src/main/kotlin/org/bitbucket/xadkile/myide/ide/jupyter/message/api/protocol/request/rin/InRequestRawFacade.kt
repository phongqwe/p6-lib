package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rin

import com.github.michaelbull.result.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.bitbucket.xadkile.myide.common.HmacMaker
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.InvalidPayloadSizeException
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.MessageHeader
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rout.OutMetaData
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.ProtocolConstant
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.InMsgContent
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rin.parser.*
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.session.Session

/**
 * For reading raw data from zmq sockets
 */
class InRequestRawFacade(
    val identities: String,
    val delimiter: String,
    val hmacSig: String,
    val header: String,
    val parentHeader: String,
    val metaData: String,
    val content: String,
    val buffer: ByteArray,
):CanVerifyHmac {
    companion object {
        /**
         * Parse a list of byte array into a [InRequestRawFacade].
         *
         * Detect the delimiter and use it as a pivot point to locate other elements
         */
        fun fromRecvPayload(payload: List<ByteArray>): Result<InRequestRawFacade, Exception> {
            if (payload.size < 6) {
                return Err(InvalidPayloadSizeException(payload.size))
            } else {
                // find the delimiter's index
                val delimiterIndexEither = findDelimiterIndex(payload)
                when (delimiterIndexEither) {
                    is Ok -> {
                        val delimiterIndex = delimiterIndexEither.value
                        val identities: String = if (delimiterIndex == 0) {
                            // does not have identities
                            ""
                        } else {
                            String(payload[delimiterIndex - 1])
                        }

                        val theRestCount: Int = payload.size - delimiterIndex

                        return Ok(InRequestRawFacade(
                            identities = identities,
                            delimiter = String(payload[delimiterIndex]),
                            hmacSig = String(payload[delimiterIndex + 1]),
                            header = String(payload[delimiterIndex + 2]),
                            parentHeader = String(payload[delimiterIndex + 3]),
                            metaData = if (theRestCount > 4) String(payload[delimiterIndex + 4]) else "",
                            content = if (theRestCount > 5) String(payload[delimiterIndex + 5]) else "",
                            buffer = if (theRestCount > 6) payload[delimiterIndex + 6].copyOf() else ByteArray(0)
                        ))
                    }
                    is Err -> {
                        return Err(delimiterIndexEither.error)
                    }
                }
            }
        }

        private fun findDelimiterIndex(
            payload: List<ByteArray>,
        ): Result<Int, NoSuchElementException> {
            try {
                val index: Int = payload.withIndex()
                    .first { (i: Int, e: ByteArray) -> String(e) == ProtocolConstant.messageDelimiter }
                    .index
                return Ok(index)
            } catch (e: NoSuchElementException) {
                return Err(
                    NoSuchElementException(
                        "Payload lacks delimiter\n" +
                                "Payload info: ${payload.map { String(it) }.joinToString("\n")}"
                    )
                )
            }
        }
    }

    inline fun <
            reified META_F : InMetaData.InFacade<META>, reified CONTENT_F : InMsgContent.Facade<CONTENT>,
            META : InMetaData, CONTENT : InMsgContent,
            > toModel(
        session: Session
    ): Result<InRequest<META, CONTENT>, Exception> {
        val o1= this.toFacade<META_F,CONTENT_F,META,CONTENT>(session)
        val rt = o1.andThen {
            it.toModel()
        }
        return rt
    }


    fun <
            META_F : InMetaData.InFacade<META>, CONTENT_F : InMsgContent.Facade<CONTENT>,
            META : InMetaData, CONTENT : InMsgContent,
            > toModel(
        metaDataFacadeParser: MetaDataInFacadeParser<META,META_F>,
        contentInFacadeParser: InMsgContentFacadeParser<CONTENT,CONTENT_F>,
        session: Session,
    ): Result<InRequest<META, CONTENT>, Exception> {
        val o1 = this.toFacade(metaDataFacadeParser, contentInFacadeParser, session)
        val rt = o1.andThen {
            it.toModel()
        }
        return rt
    }



    inline fun <
            reified META_F : InMetaData.InFacade<META>, reified CONTENT_F : InMsgContent.Facade<CONTENT>,
            META : InMetaData, CONTENT : InMsgContent,
            > toFacade(
        session: Session,
    ): Result<InRequestFacade<META,META_F,CONTENT,CONTENT_F>, Exception> {
        val gson = Gson()
        return this.toFacade(
            metaDataFacadeParser = {
                gson.fromJson(it, META_F::class.java)
            },
            contentInFacadeParser = {
                gson.fromJson(it, CONTENT_F::class.java)
            },
            session
        )
    }

    fun <
            META_F : InMetaData.InFacade<META>, CONTENT_F : InMsgContent.Facade<CONTENT>,
            META : InMetaData, CONTENT : InMsgContent,
            > toFacade(
        metaDataFacadeParser: MetaDataInFacadeParser<META,META_F>,
        contentInFacadeParser: InMsgContentFacadeParser<CONTENT,CONTENT_F>,
        session: Session,
    ): Result<InRequestFacade<META,META_F,CONTENT,CONTENT_F>, Exception> {
        val gson = Gson()
        val rt = binding<InRequestFacade<META,META_F,CONTENT,CONTENT_F>, Exception> {
            val headerObj = gson.fromJson(header, MessageHeader.Facade::class.java)
            val parentHeaderObj = gson.fromJson(parentHeader, MessageHeader.Facade::class.java)
            InRequestFacade(
                identities = identities,
                delimiter = delimiter,
                header = headerObj,
                parentHeader = parentHeaderObj,
                metadata = metaDataFacadeParser.parse(metaData),
                content = contentInFacadeParser.parse(content),
                buffer = buffer,
                key = session.key,
                session = session
            )
        }
        return rt
    }

    private fun getHMACIngredients(): List<ByteArray> {
        return listOf(
            this.header, this.parentHeader, this.metaData, this.content
        ).map { it.toByteArray(Charsets.UTF_8) }
    }

    override fun verifyHmac(key: ByteArray): Boolean {
        return this.hmacSig == HmacMaker.makeHmacSha256SigStr(key, getHMACIngredients())
    }

    override fun toString(): String {
        return GsonBuilder().setPrettyPrinting().create().toJson(this)
    }
}
