package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message

interface MsgContent {
    /**
     * must not add any function to [OutFacade]
     */
    interface OutFacade
    fun toFacade(): OutFacade

    interface InFacade {
        fun toModel():MsgContent
    }
}
