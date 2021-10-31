package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message

import org.bitbucket.xadkile.myide.common.HmacMaker

interface CanVerifyHmac {
    fun verifyHmac(key: ByteArray): Boolean
}
