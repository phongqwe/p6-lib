package com.github.xadkile.bicp.message.api.msg.listener

import com.github.xadkile.bicp.message.api.protocol.message.MsgType
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

internal class HandlerContainerImpTest {

    val h1 = MsgHandler.withUUID(msgType = MsgType.IOPub_execute_result)
    val h2 = MsgHandler.withUUID(msgType = MsgType.Shell_kernel_info_reply)
    val h3 = MsgHandler.withUUID(msgType = MsgType.Shell_kernel_info_request)
    val h4 =MsgHandler.withUUID(msgType = MsgType.IOPub_execute_result)
    var hContainer = HandlerContainerImp()

    @BeforeEach
    fun bee(){
        hContainer = HandlerContainerImp()
    }

    @Test
    fun containHandler() {
        hContainer.addHandler(h1)
        assertTrue(hContainer.containHandler(h1))
        assertTrue(hContainer.containHandler(h1.id()))
        assertFalse(hContainer.containHandler(h2))
        assertFalse(hContainer.containHandler(h2.id()))
    }

    @Test
    fun removeHandler() {
        hContainer.addHandler(h1)
        assertFalse(hContainer.isEmpty())

        hContainer.removeHandler(h1.id())
        assertTrue(hContainer.isEmpty())

        hContainer.addHandler(h1)
        hContainer.removeHandler(h1)
        assertTrue(hContainer.isEmpty())

        hContainer.addHandler(h1)
        hContainer.removeHandler(h2.msgType(),h1.id())
        assertTrue(hContainer.isNotEmpty())

        hContainer.removeHandler(h1.msgType(),h1.id())
        assertTrue(hContainer.isEmpty())
    }

    @Test
    fun addHandler_getHandlers(){

        hContainer.getHandlers(h1.msgType()).also {
            assertTrue(it.isEmpty())
        }
        // handlers are grouped by their MsgType correctly
        hContainer.addHandler(h1)
        hContainer.addHandler(h4)
        hContainer.addHandler(h2)
        hContainer.getHandlers(h1.msgType()).also {
            assertEquals(2,it.size)
            assertEquals(h1,it[0])
            assertEquals(h4,it[1])
        }
        hContainer.getHandlers(h2.msgType()).also {
            assertEquals(1,it.size)
            assertEquals(h2,  it[0])
        }
    }
}
