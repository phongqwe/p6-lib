package com.github.xadkile.bicp.message.api.protocol.message

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.xadkile.bicp.message.api.protocol.MessageHeader
import com.github.xadkile.bicp.message.api.protocol.ProtocolConstant
import com.github.xadkile.bicp.message.api.protocol.ProtocolUtils

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

        inline fun <reified META : MsgMetaData, reified CONTENT : MsgContent> fromPayload(payload:List<ByteArray>):Result<JPMessage<META,CONTENT>,Exception>{
            val rawMsg:Result<JPRawMessage,Exception> =JPRawMessage.fromPayload(payload.map { it })
            return rawMsg.map { it.toModel() }
        }
    }

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

    override fun toString(): String {
        return ProtocolUtils.msgGson.toJson(this)
    }
}

