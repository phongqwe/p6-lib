package com.github.xadkile.p6.message.api.connection.kernel_context

import com.github.michaelbull.result.*
import com.github.xadkile.p6.message.api.connection.kernel_context.exception.KernelErrors
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
    lateinit var ipythonConfig: KernelConfig
    lateinit var zContext: ZContext
    @BeforeEach
    fun beforeEach(){
        this.zContext = ZContext()
        ipythonConfig = TestResources.kernelConfigForTest()
        pm = KernelContextImp(ipythonConfig,this.zContext, GlobalScope, Dispatchers.IO)
    }

    @AfterEach
    fun afterAll(){
        runBlocking {
            pm.stopAll2()
        }
    }

    @Test
    fun testStartAndStopListeners(){
        var start = false
        pm.setKernelStartedListener {
            start = true
        }
        runBlocking{
            pm.startAll2()
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
            pm.stopAll2()
            delay(1000)
            assertTrue(afterStop)
            assertTrue(beforeStop)
        }
    }

    @Test
    fun startIPython_FromNotStartedYet() =runBlocking{
        assertTrue(pm.getKernelProcess2() is Err)
        val rs = pm.startAll2()
        assertTrue(rs is Ok,rs.toString())
        assertTrue(pm.isKernelRunning())
        assertTrue(pm.getKernelProcess2() is Ok,pm.getKernelProcess2().toString())
        assertTrue(pm.getKernelProcess2().get()?.isAlive ?: false)
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
    fun startIPython_FromAlreadyStarted() =runBlocking {
        val rs0 = pm.startAll2()
        assertTrue(rs0 is Ok)
        val rs = pm.startAll2()
        assertTrue(rs is Ok)
    }

    @Test
    fun stopIPython() =runBlocking{
        pm.startAll2()
        runBlocking {
            val rs = pm.stopAll2()
            assertTrue(rs is Ok)
        }
        assertTrue(pm.isKernelNotRunning())
        assertTrue(pm.getKernelProcess2() is Err)
        assertFalse(pm.getKernelProcess2().get()?.isAlive ?: false)
        assertTrue(pm.getConnectionFileContent() is Err)
        assertTrue(pm.getSession() is Err)
        assertTrue(pm.getChannelProvider() is Err)
        assertTrue(pm.getMsgEncoder() is Err)
        assertTrue(pm.getMsgIdGenerator() is Err)
        assertTrue(pm.getHeartBeatService() is Err)
        assertFalse(Files.exists(Paths.get(ipythonConfig.getConnectionFilePath())))
    }
    @Test
    fun stopIPython_onAlreadyStopped()=runBlocking {
        pm.startAll2()
        runBlocking {
            val rs = pm.stopAll2()
            assertTrue(rs is Ok, rs.toString())
            val rs2 = pm.stopAll2()
            assertTrue(rs2 is Ok, rs.toString())
        }
    }

    @Test
    fun restartIPython()=runBlocking {
        pm.startAll2()
        val oldConnectionFile = pm.getConnectionFileContent().get()
        assertNotNull(oldConnectionFile)
        runBlocking {
            val rs = pm.restartKernel2()
            val newConnectionFile = pm.getConnectionFileContent().get()
            assertTrue(rs is Ok, rs.toString())
            assertNotNull(newConnectionFile)
            assertNotEquals(oldConnectionFile,newConnectionFile)
        }
    }

    @Test
    fun restartIPython_OnStopped() =runBlocking{
        pm.startAll2()
        runBlocking {
            pm.stopAll2()
            val rs = pm.restartKernel2()
            assertTrue(rs is Err)
            assertTrue(rs.error.header is KernelErrors.KernelContextIllegalState)
        }
    }
}
