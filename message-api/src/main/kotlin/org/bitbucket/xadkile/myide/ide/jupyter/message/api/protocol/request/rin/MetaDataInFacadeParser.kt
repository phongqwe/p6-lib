package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rin

import com.google.gson.Gson

fun interface MetaDataInFacadeParser<META: MetaDataIn,META_F : MetaDataIn.InFacade<META>> {
    fun parse(input: String): META_F


    companion object {
        inline fun <META: MetaDataIn, reified META_F : MetaDataIn.InFacade<META>>jsonParser(): MetaDataInFacadeParser<META, META_F> {
            return MetaDataInFacadeParser{ input ->
                val gson = Gson()
                gson.fromJson(input,META_F::class.java)
            }
        }
    }
}
