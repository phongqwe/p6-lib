package org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message

import org.bitbucket.xadkile.isp.common.HmacMaker

interface CanVerifyHmac {
    fun verifyHmac(key: ByteArray): Boolean
}
