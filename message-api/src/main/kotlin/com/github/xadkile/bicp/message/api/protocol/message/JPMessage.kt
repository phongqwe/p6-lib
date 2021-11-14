package com.github.xadkile.bicp.message.api.protocol.message

import com.github.xadkile.bicp.message.api.protocol.MessageHeader
import com.github.xadkile.bicp.message.api.protocol.ProtocolConstant
import com.github.xadkile.bicp.message.api.protocol.other.ProtocolUtils

/**
 * Should message message contain key?
 * key is tied to session info.
 * message is created when it is needed, and consumed immediately and not lingering around.
 */
class JPMessage<META : MsgMetaData, CONTENT : MsgContent>(
    val identities:String,
    val delimiter:String,
    val header: MessageHeader,
    val parentHeader: MessageHeader?,
    val metadata: META?,
    val content: CONTENT,
    val buffer: ByteArray,
) {
    companion object {
        val delimiter = ProtocolConstant.messageDelimiter

        /**
         * [header] is created with its autoCreate
         * [parentHeader] is null
         * [identities] is empty
         * [metadata] is null
         * [buffer] is empty
         */
        fun <META : MsgMetaData,CONTENT : MsgContent> autoCreate(
            sessionId: String,
            username:String,
            msgType: MsgType,
            msgContent: CONTENT,
            msgId: String,
        ): JPMessage<META, CONTENT> {
            return JPMessage(
                identities = "",
                delimiter = delimiter,
                header = MessageHeader.autoCreate(
                    sessionId = sessionId,
                    username = username,
                    msgType = msgType,
                    msgId = msgId
                ),
                parentHeader = null,
                content = msgContent,
                metadata = null,
                buffer = ByteArray(0)
            )
        }

        fun <META : MsgMetaData, CONTENT : MsgContent> fromPayload(payload:List<String>):JPMessage<META,CONTENT>{
            TODO("write this")
        }
    }

    /**
     */
//    fun makePayload(key:String):List<ByteArray>{
//        val ingredients = getHMACIngredientAsStr()
//        val ingredientsAsByteArray = ingredients.map { it.toByteArray(Charsets.UTF_8) }
//        val keyAsByteArray = key.toByteArray(Charsets.UTF_8)
//        return listOf(
//            this.identities.toByteArray(Charsets.UTF_8),
//            this.delimiter.toByteArray(Charsets.UTF_8),
//            HmacMaker.makeHmacSha256SigInByteArray(keyAsByteArray,ingredientsAsByteArray),
//            ingredients[0].toByteArray(Charsets.UTF_8),
//            ingredients[1].toByteArray(Charsets.UTF_8),
//            ingredients[2].toByteArray(Charsets.UTF_8),
//            ingredients[3].toByteArray(Charsets.UTF_8),
//            this.buffer
//        )
//    }

    /**
     * Return ingredients for use in the creation a Hmac sha256 signature
     */
    fun getHMACIngredientAsStr(): List<String> {
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

