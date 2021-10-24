package org.bitbucket.xadkile.taiga.jupyterclient.client.message

import com.google.gson.GsonBuilder

class Request(
    val header: MessageHeader,
    val parent_header: MessageHeader?,
    val metadata: MetaData?,
    val content: Content,
    val buffers: List<Any> = emptyList()
) {

    /**
     * make a hmac sha256 sig from the content of this object
     * [key] is key in connection json file
     */
    private fun makeHmacSig(key: String): ByteArray {
        val keyBA = key.toByteArray(Charsets.UTF_8)
        return HMACMaker.makeHmacSha256SigInByteArray(keyBA, this.getHMACIngredientAsByteArray())
    }

    /**
     * make payload to be sent to zmq
     * [key] is key in connection json file
     */
    fun makePayload(key: String): List<ByteArray> {
        val rt = listOf(
            "<IDS|MSG>".toByteArray(Charsets.US_ASCII),
            this.makeHmacSig(key)
        ) + this.getHMACIngredientAsByteArray()
        return rt
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
        val facade = this.toFacade()
        val rt = listOf(
            gson.toJson(facade.header),
            gson.toJson(facade.parent_header),
            gson.toJson(facade.metadata),
            gson.toJson(facade.content),
        )
        return rt
    }

    fun toFacade(): Facade {
        return Facade(
            header = header.toFacade(),
            parent_header = parent_header?.toFacade() ?: EmptyJsonObj,
            metadata = metadata?.toFacade() ?: EmptyJsonObj,
            content = content.toFacade(),
            buffers = buffers
        )
    }

    data class Facade(
        val header: MessageHeader.Facade,
        val parent_header: MessageHeader.Facade,
        val metadata: MetaData.Facade,
        val content: Content.Facade,
        val buffers: List<Any>
    ) {
        private constructor() : this(EmptyJsonObj, EmptyJsonObj, EmptyJsonObj, EmptyJsonObj, emptyList())
    }
}

