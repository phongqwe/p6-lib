package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.utils

import java.util.*
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named("random")
class RandomMsgIdGenerator : MsgIdGenerator{
    override fun next(): String {
        return UUID.randomUUID().toString()
    }
}
