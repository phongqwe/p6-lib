package com.github.xadkile.bicp.message.api.protocol.message

import com.github.xadkile.bicp.common.HmacMaker

interface CanVerifyHmac {
    fun verifyHmac(key: ByteArray): Boolean
}
