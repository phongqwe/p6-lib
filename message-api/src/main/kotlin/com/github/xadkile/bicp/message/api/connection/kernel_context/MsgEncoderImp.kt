package com.github.xadkile.bicp.message.api.connection.kernel_context

import com.github.xadkile.bicp.common.HmacMaker
import com.github.xadkile.bicp.message.api.msg.protocol.message.JPMessage

class MsgEncoderImp internal constructor(val keyStr: String) : MsgEncoder {

    private val key: ByteArray = keyStr.toByteArray(Charsets.UTF_8)

    override fun encodeMessage(message: JPMessage<*, *>): List<ByteArray> {
        val ingredients: List<String> = message.getHMACIngredientAsStr()
        val ingredientsAsByteArray: List<ByteArray> = ingredients.map { it.toByteArray(Charsets.UTF_8) }
        return listOf(
            message.identities.toByteArray(Charsets.UTF_8),
            message.delimiter.toByteArray(Charsets.UTF_8),
            HmacMaker.makeHmacSha256SigInByteArray(key, ingredientsAsByteArray),
            ingredients[0].toByteArray(Charsets.UTF_8),
            ingredients[1].toByteArray(Charsets.UTF_8),
            ingredients[2].toByteArray(Charsets.UTF_8),
            ingredients[3].toByteArray(Charsets.UTF_8),
            message.buffer
        )
    }
}
