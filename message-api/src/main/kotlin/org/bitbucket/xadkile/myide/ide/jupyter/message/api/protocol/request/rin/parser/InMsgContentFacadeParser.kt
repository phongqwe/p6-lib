package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rin.parser

import com.google.gson.Gson
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.InMsgContent

fun interface InMsgContentFacadeParser<CONTENT:InMsgContent,CONTENT_F : InMsgContent.Facade<CONTENT>> {
    fun parse(input: String): CONTENT_F


    companion object {
        inline fun <
                CONTENT:InMsgContent,
                reified CONTENT_F : InMsgContent.Facade<CONTENT>,
                >
                jsonParser():InMsgContentFacadeParser<CONTENT,CONTENT_F>{
            return InMsgContentFacadeParser{ input ->
                val gson = Gson()
                gson.fromJson(input,CONTENT_F::class.java)
            }
        }
    }
}
