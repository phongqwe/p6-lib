package org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol

import arrow.core.Either
import arrow.core.computations.ResultEffect.bind
import org.bitbucket.xadkile.myide.common.HmacMaker
import org.junit.jupiter.api.Test
import zmq.io.coder.IDecoder
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue
internal class RequestFacadeTest {
    @Test
    fun zz() {
        val v1: Either<Exception, Int> = Either.Left(Exception("v1 e"))
        val v2: Either<Exception, Int> = Either.Left(Exception("v2 e"))
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
        val facade = InRequestFacade.fromRecvPayload(payload).bind()
        assertTrue(facade.verifyHmac(key))
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
        val facade = InRequestFacade.fromRecvPayload(payload)
        assertTrue(facade.isLeft())
        facade.tapLeft {
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
        val facade = InRequestFacade.fromRecvPayload(payload)
        assertTrue(facade.isLeft())
        facade.tapLeft {
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
        val facade = InRequestFacade.fromRecvPayload(payload).bind()
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
        val facade = InRequestFacade.fromRecvPayload(payload).bind()
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
        val facade = InRequestFacade.fromRecvPayload(payload).bind()
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
