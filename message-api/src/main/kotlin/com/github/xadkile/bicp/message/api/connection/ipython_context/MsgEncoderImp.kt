package com.github.xadkile.bicp.message.api.connection.ipython_context

import com.github.xadkile.bicp.common.HmacMaker
import com.github.xadkile.bicp.message.api.protocol.message.JPMessage

class MsgEncoderImp internal constructor(val key: String) : MsgEncoder {
    override fun encodeMessage(message: JPMessage<*, *>): List<ByteArray> {
            val ingredients = message.getHMACIngredientAsStr()
            val ingredientsAsByteArray = ingredients.map { it.toByteArray(Charsets.UTF_8) }
            val keyAsByteArray = key.toByteArray(Charsets.UTF_8)
            return listOf(
                message.identities.toByteArray(Charsets.UTF_8),
                message.delimiter.toByteArray(Charsets.UTF_8),
                HmacMaker.makeHmacSha256SigInByteArray(keyAsByteArray, ingredientsAsByteArray),
                ingredients[0].toByteArray(Charsets.UTF_8),
                ingredients[1].toByteArray(Charsets.UTF_8),
                ingredients[2].toByteArray(Charsets.UTF_8),
                ingredients[3].toByteArray(Charsets.UTF_8),
                message.buffer
            )
    }
}
