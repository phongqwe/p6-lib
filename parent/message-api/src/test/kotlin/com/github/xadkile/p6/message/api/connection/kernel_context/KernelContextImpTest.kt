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
    lateinit var pm : KernelContextImp
    lateinit var kernelConfig: KernelConfig
    lateinit var zContext: ZContext
    @BeforeEach
    fun beforeEach(){
        this.zContext = ZContext()
        kernelConfig = TestResources.kernelConfigForTest()
        pm = KernelContextImp(kernelConfig,this.zContext, GlobalScope, Dispatchers.IO)
    }

    @AfterEach
    fun afterAll(){
        runBlocking {
            pm.stopAll()
        }
    }

    @Test
    fun testStartAndStopListeners(){
        var start = false
        pm.setKernelStartedListener {
            start = true
        }
        runBlocking{
            pm.startAll()
        }
        assertTrue(start)
        var afterStop = false
        var beforeStop = false
        pm.setOnAfterStopListener {
            afterStop = true
        }
        pm.setOnBeforeStopListener{
            beforeStop = true
        }

        runBlocking {
            pm.stopAll()
            delay(1000)
            assertTrue(afterStop)
            assertTrue(beforeStop)
        }
    }

    @Test
    fun startIPython_FromNotStartedYet() =runBlocking{
        assertTrue(pm.getKernelProcess() is Err)
        val rs = pm.startAll()
        assertTrue(rs is Ok,rs.toString())
        assertTrue(pm.isKernelRunning())
        assertTrue(pm.getKernelProcess() is Ok,pm.getKernelProcess().toString())
        assertTrue(pm.getKernelProcess().get()?.isAlive ?: false)
        assertTrue(pm.getConnectionFileContent() is Ok,pm.getConnectionFileContent().toString())
        assertTrue(pm.getChannelProvider() is Ok,pm.getChannelProvider().toString())
        assertTrue(pm.getSession() is Ok,pm.getSession().toString())
        assertTrue(pm.getMsgEncoder() is Ok,pm.getMsgEncoder().toString())
        assertTrue(pm.getMsgIdGenerator() is Ok,pm.getMsgIdGenerator().toString())
        assertTrue(pm.getHeartBeatService() is Ok,pm.getHeartBeatService().toString())
        assertTrue(pm.getHeartBeatService().unwrap().isServiceRunning())
        assertTrue(Files.exists(Paths.get(kernelConfig.getConnectionFilePath())))
    }

    @Test
    fun startIPython_FromAlreadyStarted() =runBlocking {
        val rs0 = pm.startAll()
        assertTrue(rs0 is Ok)
        val rs = pm.startAll()
        assertTrue(rs is Ok)
    }

    @Test
    fun stopIPython() =runBlocking{
        pm.startAll()
        runBlocking {
            val rs = pm.stopAll()
            assertTrue(rs is Ok)
        }
        assertTrue(pm.isKernelNotRunning())
        assertTrue(pm.getKernelProcess() is Err)
        assertFalse(pm.getKernelProcess().get()?.isAlive ?: false)
        assertTrue(pm.getConnectionFileContent() is Err)
        assertTrue(pm.getSession() is Err)
        assertTrue(pm.getChannelProvider() is Err)
        assertTrue(pm.getMsgEncoder() is Err)
        assertTrue(pm.getMsgIdGenerator() is Err)
        assertTrue(pm.getHeartBeatService() is Err)
        assertFalse(Files.exists(Paths.get(kernelConfig.getConnectionFilePath())))
    }
    @Test
    fun stopIPython_onAlreadyStopped()=runBlocking {
        pm.startAll()
        runBlocking {
            val rs = pm.stopAll()
            assertTrue(rs is Ok, rs.toString())
            val rs2 = pm.stopAll()
            assertTrue(rs2 is Ok, rs.toString())
        }
    }

    @Test
    fun restartIPython()=runBlocking {
        pm.startAll()
        val oldConnectionFile = pm.getConnectionFileContent().get()
        assertNotNull(oldConnectionFile)
        runBlocking {
            val rs = pm.restartKernel()
            val newConnectionFile = pm.getConnectionFileContent().get()
            assertTrue(rs is Ok, rs.toString())
            assertNotNull(newConnectionFile)
            assertNotEquals(oldConnectionFile,newConnectionFile)
        }
    }

    @Test
    fun restartIPython_OnStopped() =runBlocking{
        pm.startAll()
        runBlocking {
            pm.stopAll()
            val rs = pm.restartKernel()
            assertTrue(rs is Err)
            assertTrue(rs.error.header is KernelErrors.KernelContextIllegalState)
        }
    }
}
