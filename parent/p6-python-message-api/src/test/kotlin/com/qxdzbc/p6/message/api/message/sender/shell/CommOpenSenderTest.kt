package com.qxdzbc.p6.message.api.message.sender.shell

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.unwrap
import com.qxdzbc.p6.message.api.message.protocol.data_interface_definition.Shell
import com.qxdzbc.p6.test.utils.TestOnJupyter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertTrue

internal data class SomeData(val x: Int = 1)

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CommOpenSenderTest : TestOnJupyter() {

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
        }
    }

    /**
     * See if it is feasible to bombard kernel with many request at the same time
     */
    @Test
    fun stressTest() = runBlocking {
        val message: CommOpenRequest = CommOpenRequest.autoCreate(
            sessionId = "session_id",
            username = "user_name",
            msgType = Shell.Comm.Open.msgType,
            msgContent = Shell.Comm.Open.Content(
                commId = "commId", targetName = "targetName", data = SomeData()
            ),
            kernelContext.getMsgIdGenerator().unwrap().next()
        )

        val sender = CommOpenSender(
            kernelContext = kernelContext,
            kernelServiceManager = kernelServiceManager
        )

        val o = sender.send(message)
        assertTrue(o is Ok, o.toString())
    }
}

