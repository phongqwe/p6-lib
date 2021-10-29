package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message

interface OutMsgContent {
    /**
     * must not add any function to [Facade]
     */
    interface Facade
    fun toFacade(): Facade
}
