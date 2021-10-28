package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol

import arrow.core.Either
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.binding
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.bitbucket.xadkile.myide.common.HmacMaker
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgContent
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.`in`.parser.MsgContentInFacadeParser
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.`in`.parser.MetaDataInFacadeParser
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.`in`.parser.MetaDataInParser
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.`in`.parser.MsgContentInParser
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.out.OutRequest
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
) {
    companion object {
        /**
         * Parse a list of byte array into a [InRequestRawFacade].
         *
         * Detect the delimiter and use it as a pivot point to locate other elements
         */
        fun fromRecvPayload(payload: List<ByteArray>): Either<Exception, InRequestRawFacade> {
            if (payload.size < 6) {
                return Either.Left(InvalidPayloadSizeException(payload.size))
            } else {
                // find the delimiter's index
                val delimiterIndex: Int = try {
                    payload.withIndex()
                        .first { (i: Int, e: ByteArray) -> String(e) == OutRequest.jupyterDelimiter }
                        .index
                } catch (e: NoSuchElementException) {
                    return Either.Left(
                        NoSuchElementException(
                            "Payload lacks delimiter\n" +
                                    "Payload info: ${payload.map { String(it) }.joinToString("\n")}"
                        )
                    )
                }

                val identities: String = if (delimiterIndex == 0) {
                    // does not have identities
                    ""
                } else {
                    String(payload[delimiterIndex - 1])
                }

                val theRestCount: Int = payload.size - delimiterIndex

                return Either.Right(
                    InRequestRawFacade(
                        identities = identities,
                        delimiter = String(payload[delimiterIndex]),
                        hmacSig = String(payload[delimiterIndex + 1]),
                        header = String(payload[delimiterIndex + 2]),
                        parentHeader = String(payload[delimiterIndex + 3]),
                        metaData = if (theRestCount > 4) String(payload[delimiterIndex + 4]) else "",
                        content = if (theRestCount > 5) String(payload[delimiterIndex + 5]) else "",
                        buffer = if (theRestCount > 6) payload[delimiterIndex + 6].copyOf() else ByteArray(0)
                    )
                )
            }
        }
    }

    fun <META_F : MetaData.InFacade, CONTENT_F: MsgContent.InFacade,
            META : MetaData, CONTENT : MsgContent> toModel(
        metaDataInParser: MetaDataInParser<META_F,META>,
        contentInParser: MsgContentInParser<CONTENT_F,CONTENT>,
        metaDataFacadeParser: MetaDataInFacadeParser<META_F>,
        contentInFacadeParser: MsgContentInFacadeParser<CONTENT_F>,
        session: Session,
    ): Result<InRequest<META, CONTENT>, Exception> {
        return this.toFacade(metaDataFacadeParser, contentInFacadeParser, session).andThen {
            it.toModel(metaDataInParser, contentInParser)
        }
    }

    private fun <META_F : MetaData.InFacade, CONTENT_F : MsgContent.InFacade> toFacade(
        metaDataFacadeParser: MetaDataInFacadeParser<META_F>,
        contentInFacadeParser: MsgContentInFacadeParser<CONTENT_F>,
        session: Session,
    ): Result<InRequestFacade<META_F, CONTENT_F>, Exception> {
        val gson = Gson()
        val rt = binding<InRequestFacade<META_F, CONTENT_F>, Exception> {
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

//    fun <META_F : MetaData.InFacade, CONTENT_F: MsgContent.InFacade,
//            META : MetaData, CONTENT : MsgContent> toModel(
//        metaDataInParser: (META_F)->META,
//        contentInParser: (CONTENT_F)->CONTENT,
//        metaDataFacadeParser: (String) -> META_F,
//        contentInFacadeParser: (String)-> CONTENT_F,
//        session: Session,
//    ): Result<InRequest<META, CONTENT>, Exception> {
//        return this.toFacade2(metaDataFacadeParser, contentInFacadeParser, session).andThen {
//            it.toModel(metaDataInParser, contentInParser)
//        }
//    }


//    private fun <META_F : MetaData.InFacade, CONTENT_F : MsgContent.InFacade> toFacade2(
//        metaDataFacadeParser: (String) -> META_F,
//        contentInFacadeParser: (String)-> CONTENT_F,
//        session: Session,
//    ): Result<InRequestFacade<META_F, CONTENT_F>, Exception> {
//        val gson = Gson()
//        val rt = binding<InRequestFacade<META_F, CONTENT_F>, Exception> {
//            val headerObj = gson.fromJson(header, MessageHeader.Facade::class.java)
//            val parentHeaderObj = gson.fromJson(parentHeader, MessageHeader.Facade::class.java)
//            InRequestFacade(
//                identities = identities,
//                delimiter = delimiter,
//                header = headerObj,
//                parentHeader = parentHeaderObj,
//                metadata = metaDataFacadeParser(metaData),
//                content = contentInFacadeParser(content),
//                buffer = buffer,
//                key = session.key,
//                session = session
//            )
//        }
//        return rt
//    }

    private fun getHMACIngredients(): List<ByteArray> {
        return listOf(
            this.header, this.parentHeader, this.metaData, this.content
        ).map { it.toByteArray(Charsets.UTF_8) }
    }

    fun verifyHmac(key: ByteArray): Boolean {
        return this.hmacSig == HmacMaker.makeHmacSha256SigStr(key, getHMACIngredients())
    }

    override fun toString(): String {
        return GsonBuilder().setPrettyPrinting().create().toJson(this)
    }
}
