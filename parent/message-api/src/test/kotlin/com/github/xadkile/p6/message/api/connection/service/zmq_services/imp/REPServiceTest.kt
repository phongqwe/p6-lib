package com.github.xadkile.p6.message.api.connection.service.zmq_services.imp

import com.github.michaelbull.result.Ok
import com.github.xadkile.message.api.proto.P6MsgPM.*
import com.github.xadkile.p6.message.api.connection.service.zmq_services.P6MsgHandlers
import com.github.xadkile.p6.message.api.connection.service.zmq_services.msg.P6Event
import com.github.xadkile.p6.message.api.connection.service.zmq_services.msg.P6Response
import com.github.xadkile.p6.test.utils.TestOnJupyter
import com.google.protobuf.ByteString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.zeromq.SocketType

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class REPServiceTest : TestOnJupyter() {
    val cellEvent = P6Event("code","cell_value_update")
    @Test
    fun testStandardFlow() {
        runBlocking {
            val sv = REPService(kernelContext, GlobalScope, Dispatchers.IO)
            sv.start()
            var x = 0
            var parsedMsg: P6Response? = null
            val handler = P6MsgHandlers.makeP6ResHandler { msg: P6Response ->
                x += 1
                parsedMsg = msg
            }
            sv.addHandler(cellEvent, handler)

            // send message to service
            val sendSocket = kernelContext.zContext().createSocket(SocketType.REQ)
            sendSocket.connect("tcp://localhost:${sv.zmqPort}")

            val msgProto = P6MessageProto.newBuilder()
                .setHeader(P6MessageHeaderProto.newBuilder()
                    .setMsgId("id1")
                    .setEventType(P6EventProto.newBuilder().setCode(cellEvent.code).setName(cellEvent.name).build())
                    .build())
                .setData(ByteString.copyFrom("""{"value": "cell value", "script": "cell script"}}""",Charsets.UTF_8))
                .build()

            sendSocket.send(msgProto.toByteArray())
            val rep = sendSocket.recvStr()

            assertEquals(1, x)
            assertEquals("ok", rep)
            assertEquals("id1", parsedMsg?.header?.msgId)
            assertEquals(cellEvent, parsedMsg?.header?.eventType)
            assertEquals(
                """{"value": "cell value", "script": "cell script"}}""".trimIndent(),
                parsedMsg?.data
            )
            println(parsedMsg)
            val stopRs = sv.stop()
            assertTrue(stopRs is Ok)
        }
    }
}

