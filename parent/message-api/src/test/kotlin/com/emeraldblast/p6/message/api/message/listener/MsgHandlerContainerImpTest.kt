package com.emeraldblast.p6.message.api.message.listener

import com.emeraldblast.p6.message.api.connection.service.iopub.MsgHandlerContainerImp
import com.emeraldblast.p6.message.api.connection.service.iopub.MsgHandlers
import com.emeraldblast.p6.message.api.message.protocol.MsgType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class MsgHandlerContainerImpTest {

    val h1 = MsgHandlers.withUUID(msgType = MsgType.IOPub_execute_result)
    val h2 = MsgHandlers.withUUID(msgType = MsgType.Shell_kernel_info_reply)
    val h3 = MsgHandlers.withUUID(msgType = MsgType.Shell_kernel_info_request)
    val h4 = MsgHandlers.withUUID(msgType = MsgType.IOPub_execute_result)
    var hContainer = MsgHandlerContainerImp()

    @BeforeEach
    fun bee(){
        hContainer = MsgHandlerContainerImp()
    }

    @Test
    fun containHandler() {
        hContainer.addHandler(h1)
        assertTrue(hContainer.containHandler(h1))
        assertTrue(hContainer.containHandler(h1.id))
        assertFalse(hContainer.containHandler(h2))
        assertFalse(hContainer.containHandler(h2.id))
    }

    @Test
    fun removeHandler() {
        hContainer.addHandler(h1)
        assertFalse(hContainer.isEmpty())

        hContainer.removeHandler(h1.id)
        assertTrue(hContainer.isEmpty())

        hContainer.addHandler(h1)
        hContainer.removeHandler(h1)
        assertTrue(hContainer.isEmpty())
    }

    @Test
    fun addHandler_getHandlers(){

        hContainer.getHandlers(h1.msgType).also {
            assertTrue(it.isEmpty())
        }
        // handlers are grouped by their MsgType correctly
        hContainer.addHandler(h1)
        hContainer.addHandler(h4)
        hContainer.addHandler(h2)
        hContainer.getHandlers(h1.msgType).also {
            assertEquals(2,it.size)
            assertEquals(h1,it[0])
            assertEquals(h4,it[1])
        }
        hContainer.getHandlers(h2.msgType).also {
            assertEquals(1,it.size)
            assertEquals(h2,  it[0])
        }
    }
}
