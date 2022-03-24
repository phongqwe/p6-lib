package com.github.xadkile.p6.message.api.connection.service.zmq_services.imp

import com.github.michaelbull.result.Ok
import com.github.xadkile.p6.message.api.connection.service.zmq_services.P6MsgHandlers
import com.github.xadkile.p6.message.api.connection.service.zmq_services.msg.P6Event
import com.github.xadkile.p6.message.api.connection.service.zmq_services.msg.P6Message
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
    val cell_value_update = P6Event("code","cell_value_update")
//    @Test
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
            sv.addHandler(cell_value_update,handler)

            // send message to service
            val sendSocket = kernelContext.zContext().createSocket(SocketType.REQ)
            sendSocket.connect("tcp://localhost:${sv.zmqPort}")
            val mss = """
                {"header": {"msgId": "id1", "eventType": {"name":"cell_value_update"}}, "content": {"data": "{\"value\": \"cell value\", \"script\": \"cell script\"}}"}}
            """.trimIndent()
            sendSocket.send(mss)
            val rep = sendSocket.recvStr()

            assertEquals(1,x)
            assertEquals("ok",rep)
            assertEquals("id1",parsedMsg?.header?.msgId)
            assertEquals(cell_value_update,parsedMsg?.header?.eventType)
            assertEquals(
                    """
                        {"value": "cell value", "script": "cell script"}}
                    """.trimIndent()
                ,parsedMsg?.content?.data
            )
            println(parsedMsg)
            val stopRs = sv.stop()
            assertTrue(stopRs is Ok)
        }
    }
}
