package org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.other

interface MsgIdGenerator {
    fun next():String
}
