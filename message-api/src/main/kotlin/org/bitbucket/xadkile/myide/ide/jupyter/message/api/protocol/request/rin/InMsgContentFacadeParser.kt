package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rin

import com.google.gson.Gson
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgContentIn

fun interface InMsgContentFacadeParser<CONTENT:MsgContentIn,CONTENT_F : MsgContentIn.Facade<CONTENT>> {
    fun parse(input: String): CONTENT_F


    companion object {
        inline fun <
                CONTENT:MsgContentIn,
                reified CONTENT_F : MsgContentIn.Facade<CONTENT>,
                >
                jsonParser(): InMsgContentFacadeParser<CONTENT, CONTENT_F> {
            return InMsgContentFacadeParser{ input ->
                val gson = Gson()
                gson.fromJson(input,CONTENT_F::class.java)
            }
        }
    }
}
