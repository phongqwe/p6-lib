package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rin

interface MetaDataIn {
    interface InFacade<M: MetaDataIn>{
        fun toModel(): M
    }
}
