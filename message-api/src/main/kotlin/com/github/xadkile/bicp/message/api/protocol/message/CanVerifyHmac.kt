package com.github.xadkile.bicp.message.api.protocol.message

interface CanVerifyHmac {
    fun verifyHmac(key: ByteArray): Boolean
}
