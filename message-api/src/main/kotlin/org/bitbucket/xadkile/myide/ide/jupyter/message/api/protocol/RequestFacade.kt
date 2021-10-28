package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol

import arrow.core.Either
import org.bitbucket.xadkile.myide.common.HmacMaker

/**
 * Reading directly from zmq socket
 */
class RequestFacade(
    val identities: String,
    val delimiter: String,
    val hmacSig: String,
    val header: String,
    val parentHeader: String,
    val metaData: String,
    val content: String,
    val buffer: ByteArray
) {
    companion object {
        /**
         * parse a list of byte array into a [RequestFacade]
         */
        fun fromRecvPayload(payload: List<ByteArray>): Either<Exception, RequestFacade> {
            if (payload.size < 6) {
                return Either.Left(InvalidPayloadSizeException(payload.size))
            } else {
                // find the delimiter's index
                val delimiterIndex: Int = try {
                    payload.withIndex()
                        .first { (i:Int, e:ByteArray) -> String(e) == Request.jupyterDelimiter }
                        .index
                } catch (e: NoSuchElementException) {
                    return Either.Left(
                        NoSuchElementException(
                            "Payload lacks delimiter\n" +
                            "Payload info: ${payload.map{String(it)}.joinToString("\n")}"
                        )
                    )
                }

                val identities: String = if (delimiterIndex == 0) {
                    // does not have identities
                    ""
                } else {
                    String(payload[delimiterIndex - 1])
                }

                val theRestCount:Int = payload.size - delimiterIndex

                return Either.Right(
                    RequestFacade(
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

    /**
     * Return a payload to be sent to zmq in form of a [List] of [ByteArray].
     *
     * Element order must conform to the jupyter specification: [https://jupyter-client.readthedocs.io/en/latest/messaging.html#the-wire-protocol]
     *
     * [key] is key in connection json file.
     *
     *  b'u-u-i-d',         # zmq identity(ies)
     *  b'<IDS|MSG>',       # delimiter
     *  b'baddad42',        # HMAC signature
     *  b'{header}',        # serialized header dict
     *  b'{parent_header}', # serialized parent header dict
     *  b'{metadata}',      # serialized metadata dict
     *  b'{content}',       # serialized content dict
     *  b'\xf0\x9f\x90\xb1' # extra raw data buffer(s)
     */
    fun makeSendPayload(): List<ByteArray> {
        return listOf(
            this.identities.toByteArray(Charsets.UTF_8),
            this.delimiter.toByteArray(Charsets.UTF_8),
            this.hmacSig.toByteArray(Charsets.UTF_8),
            this.header.toByteArray(Charsets.UTF_8),
            this.parentHeader.toByteArray(Charsets.UTF_8),
            this.metaData.toByteArray(Charsets.UTF_8),
            this.content.toByteArray(Charsets.UTF_8),
            this.buffer
        )
    }
}
