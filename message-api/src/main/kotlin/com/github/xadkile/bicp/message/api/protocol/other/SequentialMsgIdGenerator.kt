package com.github.xadkile.bicp.message.api.protocol.other

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgCounter

/**
 * msgId = "[uuid]_<counter>"
 */
class SequentialMsgIdGenerator(
    private val uuid: String,
    private val msgCounter: MsgCounter,
) : MsgIdGenerator {
    override fun next(): String {
        return "${uuid}_${msgCounter.next()}"
    }
}
