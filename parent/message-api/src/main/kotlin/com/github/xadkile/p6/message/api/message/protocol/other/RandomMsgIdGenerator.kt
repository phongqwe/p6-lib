package com.github.xadkile.p6.message.api.message.protocol.other

import java.util.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Named("random")
class RandomMsgIdGenerator @Inject constructor() : MsgIdGenerator{
    override fun next(): String {
        return UUID.randomUUID().toString()
    }
}
