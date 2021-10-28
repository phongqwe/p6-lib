package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol

import com.google.gson.GsonBuilder
import org.bitbucket.xadkile.myide.common.HmacMaker
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgContent
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgType
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.session.Session

// TODO consider adding identities list just in case
class Request(
    private val identities:String,
    private val delimiter:String,
    private val header: MessageHeader,
    private val parentHeader: MessageHeader?,
    private val metadata: MetaData?,
    private val content: MsgContent,
    private val buffers: ByteArray,

    private val key: String,
    private val session: Session
) {
    companion object {
        val jupyterDelimiter = "<IDS|MSG>"

        /**
         * some info is auto generated
         * [identities] is empty
         */
        fun autoCreate(session: Session, msgType: MsgType, msgContent: MsgContent, msgId: String): Request {
            return Request(
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

    fun getSessionId(): String {
        return this.session.sessionId
    }

    fun getMsgId(): String {
        return this.header.getMsgId()
    }

    fun toFacade():RequestFacade{
        val ingredients = getHMACIngredientAsStr()
        val ingredientsAsByteArray = ingredients.map { it.toByteArray(Charsets.UTF_8) }
        val keyAsByteArray = this.key.toByteArray(Charsets.UTF_8)
        return RequestFacade(
            identities=this.identities,
            delimiter=this.delimiter,
            hmacSig = HmacMaker.makeHmacSha256SigStr(keyAsByteArray,ingredientsAsByteArray),
            header =  ingredients[0],
            parentHeader=ingredients[1],
            metaData = ingredients[2],
            content=ingredients[3],
            buffer = this.buffers
        )
    }

    /**
     * Return ingredients for use in the creation a Hmac sha256 signature
     */
    private fun getHMACIngredientAsStr(): List<String> {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val rt = listOf(
            gson.toJson(this.header.toFacade()),
            this.parentHeader?.toFacade()?.let { gson.toJson(it) } ?: "{}",
            this.metadata?.toFacade()?.let { gson.toJson(it) } ?: "{}",
            gson.toJson(this.content.toFacade()),
        )
        return rt
    }
}

