package com.github.xadkile.bicp.message.api.protocol.message

import com.github.michaelbull.result.*
import com.google.gson.GsonBuilder
import com.github.xadkile.bicp.common.HmacMaker
import com.github.xadkile.bicp.message.api.protocol.InvalidPayloadSizeException
import com.github.xadkile.bicp.message.api.protocol.MessageHeader
import com.github.xadkile.bicp.message.api.protocol.ProtocolConstant
import com.github.xadkile.bicp.message.api.protocol.other.ProtocolUtils
import com.github.xadkile.bicp.message.api.connection.SessionInfo

/**
 * For reading raw data from zmq sockets
 */
class JPRawMessage(
    val identities: String,
    val delimiter: String,
    val hmacSig: String,
    val header: String,
    val parentHeader: String,
    val metaData: String,
    val content: String,
    val buffer: ByteArray,
): CanVerifyHmac {
    companion object {
        /**
         * Parse a list of byte array into a [JPRawMessage].
         *
         * Detect the delimiter and use it as a pivot point to locate other elements
         */
        fun fromRecvPayload(payload: List<ByteArray>): Result<JPRawMessage, Exception> {
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

                        return Ok(JPRawMessage(
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


    inline fun <reified META : MsgMetaData, reified CONTENT : MsgContent>
            toModel(): JPMessage<META, CONTENT> {
        val gson = ProtocolUtils.msgGson
        return JPMessage(
            identities = this.identities,
            delimiter=this.delimiter,
            header = gson.fromJson(this.header, MessageHeader::class.java),
            parentHeader = gson.fromJson(this.parentHeader,MessageHeader::class.java),
            metadata = gson.fromJson(metaData,META::class.java),
            content = gson.fromJson(content,CONTENT::class.java),
            buffer = buffer,
        )
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
