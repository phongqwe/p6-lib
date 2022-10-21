package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message

interface MsgCounter {
    fun next():Int
    fun reset()
}
