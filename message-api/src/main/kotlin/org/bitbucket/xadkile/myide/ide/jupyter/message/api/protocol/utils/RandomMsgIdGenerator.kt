package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.utils

import java.util.*

class RandomMsgIdGenerator : MsgIdGenerator{
    override fun next(): String {
        return UUID.randomUUID().toString()
    }
}
