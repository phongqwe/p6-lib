package com.github.xadkile.p6.message.api.msg.protocol

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.xadkile.p6.exception.lib.error.ErrorReport
import com.github.xadkile.p6.message.api.msg.protocol.errors.MsgProtocolErrors
import com.github.xadkile.p6.message.api.other.HmacMaker
import com.google.gson.GsonBuilder

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
) {
    companion object {
        /**
         * Parse a list of byte array into a [JPRawMessage].
         *
         * Detect the delimiter and use it as a pivot point to locate other elements
         */
        fun fromPayload2(payload: List<ByteArray>): Result<JPRawMessage, ErrorReport> {
            if (payload.size < 6) {
                val report = ErrorReport(
                    header = MsgProtocolErrors.InvalidPayloadSizeError,
                    data=MsgProtocolErrors.InvalidPayloadSizeError.Data(payload.size,6)
                )
                return Err(report)
            } else {
                // find the delimiter's index
                val delimiterIndexEither = findDelimiterIndex2(payload)
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

        private fun findDelimiterIndex2(
            payload: List<ByteArray>,
        ): Result<Int, ErrorReport> {
            try {
                val index: Int = payload.withIndex()
                    .first { (i: Int, e: ByteArray) -> String(e) == ProtocolConstant.messageDelimiter }
                    .index
                return Ok(index)
            } catch (e: NoSuchElementException) {
                return Err(
                  ErrorReport(
                      header = MsgProtocolErrors.DelimiterNotFound,
                      data = MsgProtocolErrors.DelimiterNotFound.Data(payload)
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

    fun verifyHmac(key: ByteArray): Boolean {
        return this.hmacSig == HmacMaker.makeHmacSha256SigStr(key, getHMACIngredients())
    }

    override fun toString(): String {
        return GsonBuilder().setPrettyPrinting().create().toJson(this)
    }
}
