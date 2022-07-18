package com.emeraldblast.p6.message.api.connection.service.zmq_services.imp


import com.emeraldblast.p6.message.api.connection.service.zmq_services.P6MsgHandlers
import com.emeraldblast.p6.message.api.connection.service.zmq_services.msg.P6Event
import com.emeraldblast.p6.message.api.connection.service.zmq_services.msg.P6Response
import com.emeraldblast.p6.proto.P6MsgProtos
import com.emeraldblast.p6.test.utils.TestOnJupyter
import com.github.michaelbull.result.Ok
import com.google.protobuf.ByteString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.zeromq.SocketType

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class SyncREPServiceTest : TestOnJupyter() {
    val cellEvent = P6Event("code","cell_value_update")


    @BeforeEach
    fun beforeEach(){
        this.setUp()
        runBlocking {
            kernelContext.startAll()
            kernelServiceManager.startAll()
        }
    }

    @AfterEach
    fun afterEach(){
        runBlocking {
            kernelContext.stopAll()
            kernelServiceManager.stopAll()
        }
    }

    @Test
    fun testStandardFlow() {
        runBlocking {
            val sv = SyncREPService(kernelContext, GlobalScope, Dispatchers.IO)
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

            val msgProto = P6MsgProtos.P6ResponseProto.newBuilder()
                .setHeader(P6MsgProtos.P6MessageHeaderProto.newBuilder()
                    .setMsgId("id1")
                    .setEventType(P6MsgProtos.P6EventProto.newBuilder().setCode(cellEvent.code).setName(cellEvent.name).build())
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
                ByteString.copyFrom("""{"value": "cell value", "script": "cell script"}}""",Charsets.UTF_8),
                parsedMsg?.data
            )
            println(parsedMsg)
            val stopRs = sv.stop()
            assertTrue(stopRs is Ok)
        }
    }
}

