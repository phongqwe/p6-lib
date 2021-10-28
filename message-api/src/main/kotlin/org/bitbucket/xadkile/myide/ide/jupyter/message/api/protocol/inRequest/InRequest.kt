package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.inRequest

import com.google.gson.GsonBuilder
import org.bitbucket.xadkile.myide.common.HmacMaker
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.MessageHeader
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.MetaData
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.OutRequest
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgContent
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgType
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.session.Session



class InRequest(
    private val identities:String,
    private val delimiter:String,
    private val header: MessageHeader,
    private val parentHeader: MessageHeader?,
    private val metadata: MetaData?,
    private val content: MsgContent,
    private val buffers: ByteArray,
    //
    private val key: String,
    private val session: Session
) {
    companion object {
        private val defaultDelimiter =  "<IDS|MSG>"
        /**
         * some info is auto generated
         * [identities] is empty
         */
        fun autoCreate(session: Session, msgType: MsgType, msgContent: MsgContent, msgId:String): OutRequest {
            return OutRequest(
                identities = "",
                delimiter = defaultDelimiter,
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

    fun getSessionId():String{
        return this.session.sessionId
    }

    fun getMsgId():String{
        return this.header.getMsgId()
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
    fun makePayload(): List<ByteArray> {
        val rt = mutableListOf<ByteArray>()
        rt.add(this.identities.toByteArray(Charsets.UTF_8))
        rt.add(delimiter.toByteArray(Charsets.US_ASCII))
        rt.add(this.makeHmacSig(this.key))
        rt.addAll(this.getHMACIngredientAsByteArray())
        rt.add(this.buffers)
        return rt
    }

    /**
     * Return a hmac sha256 sig from the content of this object.
     *
     * [key] is key in connection json file.
     */
    private fun makeHmacSig(key: String): ByteArray {
        val keyBA = key.toByteArray(Charsets.UTF_8)
        return HmacMaker.makeHmacSha256SigInByteArray(keyBA, this.getHMACIngredientAsByteArray())
    }

    private fun getHMACIngredientAsByteArray(): List<ByteArray> {
        return this.getHMACIngredientAsStr().map {
            it.toByteArray(Charsets.UTF_8)
        }
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

//    private fun toFacade(): Facade {
//        return Facade(
//            header = header.toFacade(),
//            parent_header = parentHeader?.toFacade() ?: MessageHeader.Facade.empty,
//            metadata = metadata?.toFacade() ?: MessageHeader.Facade.empty,
//            content = content.toFacade(),
//            buffers = buffers
//        )
//    }
//
//    /**
//     * JSON facade
//     */
//    data class Facade(
//        val header: MessageHeader.Facade,
//        val parent_header: MessageHeader.Facade,
//        val metadata: MetaData.Facade,
//        val content: Content.Facade,
//        val buffers: List<Any>
//    ) {
//        private constructor() : this(MessageHeader.Facade.empty, MessageHeader.Facade.empty, EmptyJsonObj, EmptyJsonObj, emptyList())
//    }
}
