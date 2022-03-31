package com.emeraldblast.p6.message.api.connection.service.zmq_services

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class P6MsgHandlersTest {

    @Test
    fun makeHandler() {
        val r1 = P6MsgHandlers.makeHandler {  }
        val r2 = P6MsgHandlers.makeHandler {  }
        assertNotEquals(r1.id,r2.id)
    }

}
