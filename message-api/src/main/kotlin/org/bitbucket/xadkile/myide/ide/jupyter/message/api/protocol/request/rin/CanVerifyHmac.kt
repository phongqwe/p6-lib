package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rin

import org.bitbucket.xadkile.myide.common.HmacMaker

interface CanVerifyHmac {
    fun verifyHmac(key: ByteArray): Boolean
}
