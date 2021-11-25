package com.github.xadkile.bicp.message.api.msg.protocol.message

interface CanVerifyHmac {
    fun verifyHmac(key: ByteArray): Boolean
}
