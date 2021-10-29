package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message

interface MsgContentIn {
    interface Facade <C:MsgContentIn>{
        fun toModel():C
    }
}
