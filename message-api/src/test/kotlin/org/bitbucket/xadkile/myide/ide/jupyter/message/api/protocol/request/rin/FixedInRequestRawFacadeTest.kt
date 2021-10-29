package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rin

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.unwrap
import com.google.gson.GsonBuilder
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.MessageHeader
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgContentIn
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgType
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rout.OutRequest
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.session.Session
import org.junit.jupiter.api.Test

internal class FixedInRequestRawFacadeTest {
    class InMeta(val m1: Int, val m2: String) : MetaDataIn

    class MetaFacade(val meta1: Int = 0, val meta2: String = "") : MetaDataIn.InFacade<InMeta> {
        override fun toModel(): InMeta {
            return InMeta(meta1, meta2)
        }
    }

    class Content(val data: Int, val username: String) : MsgContentIn

    class ContentFacade(val data: Int, val name: String) : MsgContentIn.Facade<Content> {
        override fun toModel(): Content {
            return Content(data, name)
        }
    }

    @Test
    fun toModel() {
        val dHeader = MessageHeader.autoCreate(MsgType.Shell.execute_request, "msgid", "s", "").toFacade()
        val gson = GsonBuilder().setPrettyPrinting().create()
        val input = listOf(
            "identities_123",
            OutRequest.jupyterDelimiter,
            "hmacSig_123",
            gson.toJson(dHeader),
            gson.toJson(dHeader),
            "{\"meta1\":1,\"meta2\":\"xxxx\"}",
            "{\"data\":123, \"name\":\"abc\"}",
            "buffer_123"
        )
        val payload = input.map { it.toByteArray(Charsets.UTF_8) }
        val facade =
            FixedInRequestRawFacade
                .fromRecvPayload<MetaFacade, ContentFacade, InMeta, Content>(payload)
                .unwrap()
        val model = facade.toModel(
            session = Session("sessionId", "abc", "somekey")
        )
        kotlin.test.assertTrue(model is Ok)
    }
}
