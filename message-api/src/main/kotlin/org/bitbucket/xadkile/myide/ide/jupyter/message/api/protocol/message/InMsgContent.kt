package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message

interface InMsgContent {
    interface Facade {
        fun toModel():InMsgContent
    }
}
