package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message

interface InMsgContent {
    interface Facade <C:InMsgContent>{
        fun toModel():C
    }
}
