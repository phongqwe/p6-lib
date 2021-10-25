package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol

import com.google.gson.GsonBuilder
import org.bitbucket.xadkile.myide.common.HMACMaker
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgContent
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgType
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.session.Session

class Request(
    private val header: MessageHeader,
    private val parentHeader: MessageHeader?,
    private val metadata: MetaData?,
    private val content: MsgContent,
    private val buffers: List<Any> = emptyList(),
    private val key: String,
    private val session: Session
) {
    companion object {
        /**
         * some info is auto generated
         */
        fun autoCreate(session: Session, msgType:MsgType, msgContent:MsgContent): Request {
            return Request(
                header = MessageHeader.autoCreate(
                    sessionId = session.sessionId,
                    username = session.username,
                    msgType = msgType,
                ),
                parentHeader = null,
                content = msgContent,
                metadata = null,
                key = session.key,
                session = session
            )
        }
    }

    /**
     * make payload to be sent to zmq
     * [key] is key in connection json file
     */
    fun makePayload(): List<ByteArray> {
        val rt = mutableListOf<ByteArray>()
        rt.add("<IDS|MSG>".toByteArray(Charsets.US_ASCII))
        rt.add(this.makeHmacSig(this.key))
        rt.addAll(this.getHMACIngredientAsByteArray())
        return rt
    }

    /**
     * make a hmac sha256 sig from the content of this object
     * [key] is key in connection json file
     */
    private fun makeHmacSig(key: String): ByteArray {
        val keyBA = key.toByteArray(Charsets.UTF_8)
        return HMACMaker.makeHmacSha256SigInByteArray(keyBA, this.getHMACIngredientAsByteArray())
    }

    private fun getHMACIngredientAsByteArray(): List<ByteArray> {
        return this.getHMACIngredientAsStr().map {
            it.toByteArray(Charsets.UTF_8)
        }
    }

    /**
     * get ingredients to create a Hmac sha256 signature
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

