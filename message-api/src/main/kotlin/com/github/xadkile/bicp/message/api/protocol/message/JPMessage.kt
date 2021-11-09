package com.github.xadkile.bicp.message.api.protocol.message

import com.github.xadkile.bicp.common.HmacMaker
import com.github.xadkile.bicp.message.api.protocol.MessageHeader
import com.github.xadkile.bicp.message.api.protocol.ProtocolConstant
import com.github.xadkile.bicp.message.api.protocol.other.ProtocolUtils
import com.github.xadkile.bicp.message.api.connection.SessionInfo


/**
 * TODO Remove key and session from this
 */
class JPMessage<META : MsgMetaData, CONTENT : MsgContent>(
    val identities:String,
    val delimiter:String,
    val header: MessageHeader,
    val parentHeader: MessageHeader?,
    val metadata: META?,
    val content: CONTENT,
    val buffer: ByteArray,
    val key: String,
    val session: SessionInfo
):PayloadMaker {
    companion object {
        val delimiter = ProtocolConstant.messageDelimiter

        /**
         * [header] is created with its autoCreate
         * [parentHeader] is null
         * [identities] is empty
         * [metadata] is null
         * [buffer] is empty
         */
        fun <CONTENT : MsgContent> autoCreate(session: SessionInfo, msgType: MsgType, msgContent: CONTENT, msgId: String): JPMessage<MsgMetaData, CONTENT> {
            return JPMessage(
                identities = "",
                delimiter = delimiter,
                header = MessageHeader.autoCreate(
                    sessionId = session.sessionId,
                    username = session.username,
                    msgType = msgType,
                    msgId = msgId
                ),
                parentHeader = null,
                content = msgContent,
                metadata = null,
                key = session.key,
                session = session,
                buffer = ByteArray(0)
            )
        }
        fun <META : MsgMetaData, CONTENT : MsgContent> fromPayload(payload:List<String>):JPMessage<META,CONTENT>{
            TODO("write this")
        }
    }

    /**
     * TODO inject key here
     */
    override fun makePayload():List<ByteArray>{
        val ingredients = getHMACIngredientAsStr()
        val ingredientsAsByteArray = ingredients.map { it.toByteArray(Charsets.UTF_8) }
        val keyAsByteArray = this.key.toByteArray(Charsets.UTF_8)
        return listOf(
            this.identities.toByteArray(Charsets.UTF_8),
            this.delimiter.toByteArray(Charsets.UTF_8),
            HmacMaker.makeHmacSha256SigInByteArray(keyAsByteArray,ingredientsAsByteArray),
            ingredients[0].toByteArray(Charsets.UTF_8),
            ingredients[1].toByteArray(Charsets.UTF_8),
            ingredients[2].toByteArray(Charsets.UTF_8),
            ingredients[3].toByteArray(Charsets.UTF_8),
            this.buffer
        )
    }

    /**
     * Return ingredients for use in the creation a Hmac sha256 signature
     */
    private fun getHMACIngredientAsStr(): List<String> {
        val gson = ProtocolUtils.msgGson
        val rt = listOf(
            gson.toJson(this.header),
            this.parentHeader?.let { gson.toJson(it) } ?: "{}",
            this.metadata?.let { gson.toJson(it) } ?: "{}",
            gson.toJson(this.content),
        )
        return rt
    }
}

