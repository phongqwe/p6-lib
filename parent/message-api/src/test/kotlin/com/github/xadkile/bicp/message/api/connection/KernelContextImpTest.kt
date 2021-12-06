package com.github.xadkile.bicp.message.api.connection

import com.github.michaelbull.result.*
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelConfig
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelContextImp
import com.github.xadkile.bicp.test.utils.TestResources
import kotlinx.coroutines.GlobalScope
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
    lateinit var ipythonConfig: KernelConfig
    lateinit var zContext: ZContext
    @BeforeEach
    fun beforeEach(){
        this.zContext = ZContext()
        ipythonConfig = TestResources.kernelConfigForTest()
        pm = KernelContextImp(ipythonConfig,this.zContext, GlobalScope)
    }

    @AfterEach
    fun afterAll(){
        runBlocking {
            pm.stopKernel()
        }
    }

    @Test
    fun testStartAndStopListeners(){
        var start = false
        pm.setKernelStartedListener {
            start = true
        }
        pm.startKernel()
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
            pm.stopKernel()
            assertTrue(afterStop)
            assertTrue(beforeStop)
        }
    }

    @Test
    fun startIPython_FromNotStartedYet() {
        assertTrue(pm.getIPythonProcess() is Err)
        val rs = pm.startKernel()
        assertTrue(rs is Ok)
        assertTrue(pm.isKernelRunning())
        assertTrue(pm.getIPythonProcess() is Ok,pm.getIPythonProcess().toString())
        assertTrue(pm.getIPythonProcess().get()?.isAlive ?: false)
        assertTrue(pm.getConnectionFileContent() is Ok,pm.getConnectionFileContent().toString())
        assertTrue(pm.getChannelProvider() is Ok,pm.getChannelProvider().toString())
        assertTrue(pm.getSession() is Ok,pm.getSession().toString())
        assertTrue(pm.getMsgEncoder() is Ok,pm.getMsgEncoder().toString())
        assertTrue(pm.getMsgIdGenerator() is Ok,pm.getMsgIdGenerator().toString())
        assertTrue(pm.getHeartBeatService() is Ok,pm.getHeartBeatService().toString())
        assertTrue(pm.getHeartBeatService().unwrap().isServiceRunning())
        assertTrue(Files.exists(Paths.get(ipythonConfig.getConnectionFilePath())))
    }

    @Test
    fun startIPython_FromAlreadyStarted() {
        val rs0 = pm.startKernel()
        assertTrue(rs0 is Ok)
        val rs = pm.startKernel()
        assertTrue(rs is Ok)
    }

    @Test
    fun stopIPython() {
        pm.startKernel()
        runBlocking {
            val rs = pm.stopKernel()
            assertTrue(rs is Ok)
        }
        assertTrue(pm.isKernelNotRunning())
        assertTrue(pm.getIPythonProcess() is Err)
        assertFalse(pm.getIPythonProcess().get()?.isAlive ?: false)
        assertTrue(pm.getConnectionFileContent() is Err)
        assertTrue(pm.getSession() is Err)
        assertTrue(pm.getChannelProvider() is Err)
        assertTrue(pm.getMsgEncoder() is Err)
        assertTrue(pm.getMsgIdGenerator() is Err)
        assertTrue(pm.getHeartBeatService() is Err)
        assertFalse(Files.exists(Paths.get(ipythonConfig.getConnectionFilePath())))
    }
    @Test
    fun stopIPython_onAlreadyStopped() {
        pm.startKernel()
        runBlocking {
            val rs = pm.stopKernel()
            assertTrue(rs is Ok, rs.toString())
            val rs2 = pm.stopKernel()
            assertTrue(rs2 is Ok, rs.toString())
        }
    }

    @Test
    fun restartIPython() {
        pm.startKernel()
        val oldConnectionFile = pm.getConnectionFileContent().get()
        assertNotNull(oldConnectionFile)
        runBlocking {
            val rs = pm.restartKernel()
            val newConnectionFile = pm.getConnectionFileContent().get()
            assertTrue(rs is Ok)
            assertNotNull(newConnectionFile)
            assertNotEquals(oldConnectionFile,newConnectionFile)
        }
    }

    @Test
    fun restartIPython_OnStopped() {
        pm.startKernel()
        runBlocking {
            pm.stopKernel()
            val rs = pm.restartKernel()
            assertTrue(rs is Err)
            assertTrue(rs.getError() is IllegalStateException)
        }
    }
}
