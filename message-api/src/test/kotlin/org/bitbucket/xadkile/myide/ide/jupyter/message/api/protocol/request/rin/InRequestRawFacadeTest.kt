package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rin

import com.github.michaelbull.result.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.bitbucket.xadkile.myide.common.HmacMaker
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.InvalidPayloadSizeException
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.MessageHeader
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.InMsgContent
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgType
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rin.parser.InMetaData
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.request.rout.OutRequest
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.session.Session
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class InRequestRawFacadeTest {
    class InMeta(val m1: Int, val m2: String) : InMetaData {
    }

    class MetaFacade(val meta1: Int = 0, val meta2: String = "") : InMetaData.InFacade<InMeta> {
        override fun toModel(): InMeta {
            return InMeta(meta1, meta2)
        }
    }

    class Content(val data: Int, val username: String) : InMsgContent

    class ContentFacade(val data: Int, val name: String) : InMsgContent.Facade<Content> {
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
        val facade = InRequestRawFacade.fromRecvPayload(payload).unwrap()
        val model = facade.toModel<MetaFacade, ContentFacade,InMeta,Content>(
            session = Session("sessionId", "abc", "somekey")
        )
        val facadeInternal = facade.toFacade<MetaFacade, ContentFacade,InMeta,Content>(session = Session("sessionId", "abc", "somekey"))
        assertTrue(model is Ok)
        println(gson.toJson(facadeInternal))

    }


    @Test
    fun verifyHmac() {
        val key = "1234".toByteArray()
        val hmacSig = HmacMaker.makeHmacSha256SigStr(key, listOf(
            "header_123",
            "parentHeader_123",
            "metadata_123",
            "content_123",
        ).map { it.toByteArray(Charsets.UTF_8) })
        val input = listOf(
            "identities_123",
            OutRequest.jupyterDelimiter,
            hmacSig,
            "header_123",
            "parentHeader_123",
            "metadata_123",
            "content_123",
            "buffer_123"
        )
        val payload = input.map { it.toByteArray(Charsets.UTF_8) }
        val facade = InRequestRawFacade.fromRecvPayload(payload).get()
        assertTrue(facade?.verifyHmac(key) ?: false)
    }

    @Test
    fun fromRecvPayload_complete_wrongSize() {
        val input = listOf(
            "identities_123",
            OutRequest.jupyterDelimiter + "wrong___",
            "hmacSig_123",
            "header_123",
        )
        val payload = input.map { it.toByteArray(Charsets.UTF_8) }
        val facade = InRequestRawFacade.fromRecvPayload(payload)
        assertTrue(facade is Err)
        facade.onFailure {
            assertTrue(it is InvalidPayloadSizeException)
        }
    }

    @Test
    fun fromRecvPayload_complete_wrongDelimiter() {
        val input = listOf(
            "identities_123",
            OutRequest.jupyterDelimiter + "wrong___",
            "hmacSig_123",
            "header_123",
            "parentHeader_123",
            "metadata_123",
            "content_123",
            "buffer_123"
        )
        val payload = input.map { it.toByteArray(Charsets.UTF_8) }
        val facade = InRequestRawFacade.fromRecvPayload(payload)
        assertTrue(facade is Err)
        facade.onFailure {
            assertTrue(it is NoSuchElementException)
        }
    }

    @Test
    fun fromRecvPayload_complete() {
        val input = listOf(
            "identities_123",
            OutRequest.jupyterDelimiter,
            "hmacSig_123",
            "header_123",
            "parentHeader_123",
            "metadata_123",
            "content_123",
            "buffer_123"
        )
        val payload = input.map { it.toByteArray(Charsets.UTF_8) }
        val facade = InRequestRawFacade.fromRecvPayload(payload).unwrap()
        assertEquals(input[0], facade.identities)
        assertEquals(input[1], facade.delimiter)
        assertEquals(input[2], facade.hmacSig)
        assertEquals(input[3], facade.header)
        assertEquals(input[4], facade.parentHeader)
        assertEquals(input[5], facade.metaData)
        assertEquals(input[6], facade.content)
        assertTrue(Arrays.equals(input[7].toByteArray(), facade.buffer))
    }

    @Test
    fun fromRecvPayload_noId() {
        val input = listOf(
            OutRequest.jupyterDelimiter,
            "hmacSig_123",
            "header_123",
            "parentHeader_123",
            "metadata_123",
            "content_123",
            "buffer_123"
        )
        val payload = input.map { it.toByteArray(Charsets.UTF_8) }
        val facade = InRequestRawFacade.fromRecvPayload(payload).unwrap()
        assertEquals("", facade.identities)
        assertEquals(input[0], facade.delimiter)
        assertEquals(input[1], facade.hmacSig)
        assertEquals(input[2], facade.header)
        assertEquals(input[3], facade.parentHeader)
        assertEquals(input[4], facade.metaData)
        assertEquals(input[5], facade.content)
        assertTrue(Arrays.equals(input[6].toByteArray(), facade.buffer))
    }

    @Test
    fun fromRecvPayload_noId_noBuffer() {
        val input = listOf(
            OutRequest.jupyterDelimiter,
            "hmacSig_123",
            "header_123",
            "parentHeader_123",
            "metadata_123",
            "content_123",
        )
        val payload = input.map { it.toByteArray(Charsets.UTF_8) }
        val facade = InRequestRawFacade.fromRecvPayload(payload).unwrap()
        assertEquals("", facade.identities)
        assertEquals("", facade.identities)
        assertEquals(input[0], facade.delimiter)
        assertEquals(input[1], facade.hmacSig)
        assertEquals(input[2], facade.header)
        assertEquals(input[3], facade.parentHeader)
        assertEquals(input[4], facade.metaData)
        assertEquals(input[5], facade.content)
        assertTrue(facade.buffer.isEmpty())
    }
}
