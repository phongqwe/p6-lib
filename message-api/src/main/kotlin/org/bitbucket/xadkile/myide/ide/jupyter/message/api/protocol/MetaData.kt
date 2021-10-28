package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol


interface MetaData {

    interface OutFacade
    interface InFacade{
        fun toModel():MetaData
    }
    fun toFacade(): OutFacade
}
