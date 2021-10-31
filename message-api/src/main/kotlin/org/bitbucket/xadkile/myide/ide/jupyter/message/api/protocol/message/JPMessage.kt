package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message

import org.bitbucket.xadkile.myide.common.HmacMaker
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.MessageHeader
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.ProtocolConstant
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.utils.ProtocolUtils
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.session.Session

class JPMessage<META : MsgMetaData, CONTENT : MsgContent>(
    val identities:String,
    val delimiter:String,
    val header: MessageHeader,
    val parentHeader: MessageHeader?,
    val metadata: META?,
    val content: CONTENT,
    val buffers: ByteArray,
    val key: String,
    val session: Session
) {
    companion object {
        val jupyterDelimiter = ProtocolConstant.messageDelimiter

        /**
         * some info is auto generated
         * [identities] is empty
         */
        fun <CONTENT : MsgContent> autoCreate(session: Session, msgType: MsgType, msgContent: CONTENT, msgId: String): JPMessage<MsgMetaData, CONTENT> {
            return JPMessage(
                identities = "",
                delimiter = jupyterDelimiter,
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
                buffers = ByteArray(0)
            )
        }
    }




    fun makePayload():List<ByteArray>{
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
            this.buffers
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

