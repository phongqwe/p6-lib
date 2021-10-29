package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rin.parser

import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rout.OutMetaData

interface InMetaData {
    interface InFacade{
        fun toModel(): InMetaData
    }
}
