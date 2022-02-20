package com.github.xadkile.p6.message.api.connection.service.zmq_services.imp

import com.github.michaelbull.result.Ok
import com.github.xadkile.p6.message.api.connection.service.zmq_services.P6MsgHandlers
import com.github.xadkile.p6.message.api.connection.service.zmq_services.msg.P6Message
import com.github.xadkile.p6.message.api.connection.service.zmq_services.msg.P6MsgType
import com.github.xadkile.p6.test.utils.TestOnJupyter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.zeromq.SocketType

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class REPServiceTest : TestOnJupyter(){
    @Test
    fun testStandardFlow(){
        runBlocking {
            val sv = REPService(kernelContext,GlobalScope, Dispatchers.IO)
            sv.start()
            var x = 0
            var parsedMsg:P6Message? = null
            val handler = P6MsgHandlers.makeHandler { msg:P6Message->
                x+=1
                parsedMsg =msg
            }
            sv.addHandler(P6MsgType.cell_value_update,handler)
            val socket = kernelContext.zContext().createSocket(SocketType.REQ)
            socket.connect("tcp://localhost:${sv.zmqPort}")
            val mss = """
                {"header": {"msgId": "id1", "msgType": "cell_value_update"}, "content": {"data": "{\"value\": \"cell value\", \"script\": \"cell script\"}}"}}
            """.trimIndent()
            socket.send(mss)
            val rep = socket.recvStr()
            sv.stop()
            assertEquals(1,x)
            assertEquals("ok",rep)

            assertEquals("id1",parsedMsg?.header?.msgId)
            assertEquals(P6MsgType.cell_value_update,parsedMsg?.header?.msgType)
            assertEquals(
                    """
                        {"value": "cell value", "script": "cell script"}}
                    """.trimIndent()
                ,parsedMsg?.content?.data
            )
            val stopRs = sv.stop()
            assertTrue(stopRs is Ok)
        }
    }
}
