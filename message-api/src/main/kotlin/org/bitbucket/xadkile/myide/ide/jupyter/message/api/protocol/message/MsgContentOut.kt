package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message

interface MsgContentOut {
    /**
     * must not add any function to [Facade]
     */
    interface Facade
    fun toFacade(): Facade
}
