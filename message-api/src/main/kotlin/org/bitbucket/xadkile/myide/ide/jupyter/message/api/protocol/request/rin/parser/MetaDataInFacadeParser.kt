package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rin.parser

import com.google.gson.Gson
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rout.OutMetaData

fun interface MetaDataInFacadeParser<META:InMetaData,META_F : InMetaData.InFacade<META>> {
    fun parse(input: String): META_F


    companion object {
        inline fun <META:InMetaData, reified META_F : InMetaData.InFacade<META>>jsonParser():MetaDataInFacadeParser<META,META_F>{
            return MetaDataInFacadeParser{ input ->
                val gson = Gson()
                gson.fromJson(input,META_F::class.java)
            }
        }
    }
}
