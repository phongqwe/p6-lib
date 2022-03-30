package com.github.xadkile.p6.message.api.connection.kernel_context

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.get
import com.github.michaelbull.result.unwrap
import com.github.xadkile.p6.message.api.connection.kernel_context.errors.KernelErrors
import com.github.xadkile.p6.test.utils.TestResources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.zeromq.ZContext
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class KernelContextImpTest {
    lateinit var kc: KernelContextImp
    lateinit var kernelConfig: KernelConfig
    lateinit var zContext: ZContext

    @BeforeEach
    fun beforeEach() {
        this.zContext = ZContext()
        kernelConfig = TestResources.kernelConfigForTest()
        kc = KernelContextImp(kernelConfig, this.zContext, GlobalScope, Dispatchers.IO)
    }

    @AfterEach
    fun afterAll() {
        runBlocking {
            kc.stopAll()
        }
    }

    @Test
    fun testStartAndStopListeners() {
        var start = false
        kc.setKernelStartedListener {
            start = true
        }
        runBlocking {
            kc.startAll()
        }
        assertTrue(start)
        var afterStop = false
        var beforeStop = false
        kc.setOnAfterStopListener {
            afterStop = true
        }
        kc.setOnBeforeStopListener {
            beforeStop = true
        }

        runBlocking {
            kc.stopAll()
            delay(1000)
            assertTrue(afterStop)
            assertTrue(beforeStop)
        }
    }

    @Test
    fun startIPython_FromNotStartedYet() = runBlocking {
        assertTrue(kc.getKernelProcess() is Err)
        val rs = kc.startAll()
        assertTrue(rs is Ok, rs.toString())
        assertTrue(kc.isKernelRunning())
        assertTrue(kc.getKernelProcess() is Ok, kc.getKernelProcess().toString())
        assertTrue(kc.getKernelProcess().get()?.isAlive ?: false)
        assertTrue(kc.getConnectionFileContent() is Ok, kc.getConnectionFileContent().toString())
        assertTrue(kc.getChannelProvider() is Ok, kc.getChannelProvider().toString())
        assertTrue(kc.getSession() is Ok, kc.getSession().toString())
        assertTrue(kc.getMsgEncoder() is Ok, kc.getMsgEncoder().toString())
        assertTrue(kc.getMsgIdGenerator() is Ok, kc.getMsgIdGenerator().toString())
        assertTrue(kc.getHeartBeatService() is Ok, kc.getHeartBeatService().toString())
        assertTrue(kc.getHeartBeatService().unwrap().isRunning())
        assertTrue(kc.getZmqREPService() is Ok)
        assertTrue(kc.getZmqREPService().unwrap().isRunning())
        assertTrue(kc.getIOPubListenerService() is Ok)
        assertTrue(kc.getIOPubListenerService().unwrap().isRunning())
        assertTrue(Files.exists(Paths.get(kernelConfig.getConnectionFilePath())))
    }

    @Test
    fun startIPython_FromAlreadyStarted() = runBlocking {
        val rs0 = kc.startAll()
        assertTrue(rs0 is Ok)
        val rs = kc.startAll()
        assertTrue(rs is Ok)
    }

    @Test
    fun stopIPython() = runBlocking {
        kc.startAll()
        runBlocking {
            val rs = kc.stopAll()
            assertTrue(rs is Ok)
        }
        assertTrue(kc.isKernelNotRunning())
        assertTrue(kc.getKernelProcess() is Err)
        assertFalse(kc.getKernelProcess().get()?.isAlive ?: false)
        assertTrue(kc.getConnectionFileContent() is Err)
        assertTrue(kc.getSession() is Err)
        assertTrue(kc.getChannelProvider() is Err)
        assertTrue(kc.getMsgEncoder() is Err)
        assertTrue(kc.getMsgIdGenerator() is Err)
        assertTrue(kc.getHeartBeatService() is Err)
        assertTrue(kc.getZmqREPService() is Err)
        assertTrue(kc.getIOPubListenerService() is Err)
        assertFalse(Files.exists(Paths.get(kernelConfig.getConnectionFilePath())))
    }

    @Test
    fun stopIPython_onAlreadyStopped() = runBlocking {
        kc.startAll()
        runBlocking {
            val rs = kc.stopAll()
            assertTrue(rs is Ok, rs.toString())
            val rs2 = kc.stopAll()
            assertTrue(rs2 is Ok, rs.toString())
        }
    }

    @Test
    fun restartIPython() = runBlocking {
        kc.startAll()
        val oldConnectionFile = kc.getConnectionFileContent().get()
        assertNotNull(oldConnectionFile)
        runBlocking {
            val rs = kc.restartKernel()
            val newConnectionFile = kc.getConnectionFileContent().get()
            assertTrue(rs is Ok, rs.toString())
            assertNotNull(newConnectionFile)
            assertNotEquals(oldConnectionFile, newConnectionFile)
        }
    }

    @Test
    fun restartIPython_OnStopped() = runBlocking {
        kc.startAll()
        runBlocking {
            kc.stopAll()
            val rs = kc.restartKernel()
            assertTrue(rs is Err)
            assertTrue(rs.error.header is KernelErrors.KernelContextIllegalState)
        }
    }
}
