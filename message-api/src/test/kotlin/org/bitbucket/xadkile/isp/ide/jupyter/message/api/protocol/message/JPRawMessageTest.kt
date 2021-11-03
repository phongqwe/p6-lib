package org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message

import com.github.michaelbull.result.*
import com.google.gson.annotations.SerializedName
import org.bitbucket.xadkile.isp.common.HmacMaker
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.InvalidPayloadSizeException
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.MessageHeader
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.other.ProtocolUtils
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.session.SessionInfo
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class JPRawMessageTest {
    data class Meta(@SerializedName("meta1") val m1: Int, @SerializedName("meta2")val m2: String) :
        MsgMetaData

    data class Content(val data: Int, @SerializedName("name")val username: String) : MsgContent

    @Test
    fun toModel() {
        val dHeader = MessageHeader.autoCreate(MsgType.Shell_execute_request, "msgid", "session123", "abc")
        val gson = ProtocolUtils.msgGson
        val payload = listOf(
            "identities_123",
            JPMessage.delimiter,
            "hmacSig_123",
            gson.toJson(dHeader),
            gson.toJson(dHeader),
            "{\"meta1\":1,\"meta2\":\"xxxx\"}",
            "{\"data\":123, \"name\":\"abc\"}",
            "buffer_123"
        ).map { it.toByteArray(Charsets.UTF_8) }
        val facade =
            JPRawMessage
                .fromRecvPayload(payload)
                .unwrap()
        val model: JPMessage<Meta, Content> = facade.toModel<Meta, Content>(
            session = SessionInfo("sessionId", "abc", "somekey")
        )
        assertEquals(Content(123,"abc"),model.content)
        assertEquals(Meta(1,"xxxx"),model.metadata)
        assertEquals("identities_123",model.identities)
        assertEquals(JPMessage.delimiter,model.delimiter)
        assertEquals(dHeader,model.parentHeader)
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
            JPMessage.delimiter,
            hmacSig,
            "header_123",
            "parentHeader_123",
            "metadata_123",
            "content_123",
            "buffer_123"
        )
        val payload = input.map { it.toByteArray(Charsets.UTF_8) }
        val facade = JPRawMessage.fromRecvPayload(payload).get()
        assertTrue(facade?.verifyHmac(key) ?: false)
    }

    @Test
    fun fromRecvPayload_complete_wrongSize() {
        val input = listOf(
            "identities_123",
            JPMessage.delimiter + "wrong___",
            "hmacSig_123",
            "header_123",
        )
        val payload = input.map { it.toByteArray(Charsets.UTF_8) }
        val facade = JPRawMessage.fromRecvPayload(payload)
        assertTrue(facade is Err)
        facade.onFailure {
            assertTrue(it is InvalidPayloadSizeException)
        }
    }

    @Test
    fun fromRecvPayload_complete_wrongDelimiter() {
        val input = listOf(
            "identities_123",
            JPMessage.delimiter + "wrong___",
            "hmacSig_123",
            "header_123",
            "parentHeader_123",
            "metadata_123",
            "content_123",
            "buffer_123"
        )
        val payload = input.map { it.toByteArray(Charsets.UTF_8) }
        val facade = JPRawMessage.fromRecvPayload(payload)
        assertTrue(facade is Err)
        facade.onFailure {
            assertTrue(it is NoSuchElementException)
        }
    }

    @Test
    fun fromRecvPayload_complete() {
        val input = listOf(
            "identities_123",
            JPMessage.delimiter,
            "hmacSig_123",
            "header_123",
            "parentHeader_123",
            "metadata_123",
            "content_123",
            "buffer_123"
        )
        val payload = input.map { it.toByteArray(Charsets.UTF_8) }
        val facade = JPRawMessage.fromRecvPayload(payload).unwrap()
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
            JPMessage.delimiter,
            "hmacSig_123",
            "header_123",
            "parentHeader_123",
            "metadata_123",
            "content_123",
            "buffer_123"
        )
        val payload = input.map { it.toByteArray(Charsets.UTF_8) }
        val facade = JPRawMessage.fromRecvPayload(payload).unwrap()
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
            JPMessage.delimiter,
            "hmacSig_123",
            "header_123",
            "parentHeader_123",
            "metadata_123",
            "content_123",
        )
        val payload = input.map { it.toByteArray(Charsets.UTF_8) }
        val facade = JPRawMessage.fromRecvPayload(payload).unwrap()
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
