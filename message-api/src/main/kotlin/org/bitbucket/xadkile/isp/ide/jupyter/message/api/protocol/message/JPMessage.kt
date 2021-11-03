package org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message

import org.bitbucket.xadkile.isp.common.HmacMaker
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.MessageHeader
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.ProtocolConstant
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.other.ProtocolUtils
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.session.SessionInfo

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
    }

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

