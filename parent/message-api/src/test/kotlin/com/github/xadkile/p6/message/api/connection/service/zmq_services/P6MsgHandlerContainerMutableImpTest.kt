package com.github.xadkile.p6.message.api.connection.service.zmq_services

import com.github.xadkile.p6.message.api.connection.service.zmq_services.msg.P6EventType
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeEach
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class P6MsgHandlerContainerMutableImpTest {
    lateinit var container:P6MsgHandlerContainerMutableImp
    lateinit var containerFilled:P6MsgHandlerContainerMutableImp
    val reactors = listOf(
        P6MsgHandlers.makeHandler {  },P6MsgHandlers.makeHandler {  },
        P6MsgHandlers.makeHandler {  },P6MsgHandlers.makeHandler {  },
        P6MsgHandlers.makeHandler {  },
    )
    val notContainedReactors = listOf(
        P6MsgHandlers.makeHandler {  },P6MsgHandlers.makeHandler {  },
        P6MsgHandlers.makeHandler {  },P6MsgHandlers.makeHandler {  },
    )
    @BeforeEach
    fun b(){
        container = P6MsgHandlerContainerMutableImp()
        containerFilled = P6MsgHandlerContainerMutableImp()

        containerFilled.addHandler(P6EventType.worksheet_update,reactors[0])
        containerFilled.addHandler(P6EventType.worksheet_update,reactors[1])
        containerFilled.addHandler(P6EventType.cell_value_update,reactors[2])
        containerFilled.addHandler(P6EventType.cell_value_update,reactors[3])
        containerFilled.addHandler(P6EventType.cell_value_update,reactors[4])
    }

    @Test
    fun addHandler() {
        assertTrue(container.isEmpty())
        val reactor = P6MsgHandlers.makeHandler {  }
        container.addHandler(P6EventType.cell_value_update,reactor)
        assertTrue(container.isNotEmpty())
        val gotReactor = container.getHandler(reactor.id)
        assertEquals(reactor,gotReactor)
    }

    @Test
    fun addHandler_mutlipleReactor() {
        assertTrue(container.isEmpty())
        val reactor = P6MsgHandlers.makeHandler {  }
        val reactor2 = P6MsgHandlers.makeHandler {  }
        container.addHandler(P6EventType.cell_value_update,reactor)

        assertTrue(container.isNotEmpty())
        val gotReactor = container.getHandler(reactor.id)
        var gotReactor2 = container.getHandler(reactor2.id)
        assertEquals(reactor,gotReactor)
        assertEquals(null,gotReactor2)
        container.addHandler(P6EventType.worksheet_update,reactor2)
        gotReactor2 = container.getHandler(reactor2.id)
        assertEquals(reactor2,gotReactor2)
    }

    @Test
    fun getHandler(){
        for(reactor in reactors){
            assertNotNull(containerFilled.getHandler(reactor.id))
        }

        for(reactor in notContainedReactors){
            assertNull(containerFilled.getHandler(reactor.id))
        }
    }

    @Test
    fun removeHandler() {
        assertNotNull(this.containerFilled.getHandler(reactors[0].id))
        this.containerFilled.removeHandler(reactors[0].id)
        assertNull(this.containerFilled.getHandler(reactors[0].id))
    }

    @Test
    fun getHandlerByMsgType() {
        val rl = containerFilled.getHandlerByMsgType(P6EventType.cell_value_update)
        assertEquals(3,rl.size)
        assertTrue(rl.contains(reactors[2]))
        assertTrue(rl.contains(reactors[3]))
        assertTrue(rl.contains(reactors[4]))
        val rl2 = containerFilled.getHandlerByMsgType(P6EventType.worksheet_update)
        assertEquals(2,rl2.size)
        assertTrue(rl2.contains(reactors[0]))
        assertTrue(rl2.contains(reactors[1]))
    }

    @Test
    fun removeHandlerForMsgType() {
        containerFilled.removeHandlerForMsgType(P6EventType.cell_value_update)
        val rl = containerFilled.getHandlerByMsgType(P6EventType.cell_value_update)
        assertTrue(rl.isEmpty())
        val rl2 = containerFilled.getHandlerByMsgType(P6EventType.worksheet_update)
        assertTrue(rl2.isNotEmpty())
    }

    @Test
    fun testRemoveHandler() {
        containerFilled.removeHandler(reactors[0].id)
        assertNull(containerFilled.getHandler(reactors[0].id))
        assertNotNull(containerFilled.getHandler(reactors[1].id))
        assertNotNull(containerFilled.getHandler(reactors[2].id))
        assertNotNull(containerFilled.getHandler(reactors[3].id))
        assertNotNull(containerFilled.getHandler(reactors[4].id))

        containerFilled.removeHandler(P6EventType.cell_value_update,reactors[2].id)
        assertNull(containerFilled.getHandler(reactors[2].id))
        assertNotNull(containerFilled.getHandler(reactors[1].id))
        assertNotNull(containerFilled.getHandler(reactors[3].id))
        assertNotNull(containerFilled.getHandler(reactors[4].id))

    }

}
