package com.github.xadkile.bicp.message.api.connection

import com.github.michaelbull.result.*
import com.github.xadkile.bicp.message.api.connection.ipython_context.IPythonConfig
import com.github.xadkile.bicp.message.api.connection.ipython_context.IPythonContextImp
import com.github.xadkile.bicp.test.utils.TestResource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.zeromq.ZContext
import java.nio.file.Files
import java.nio.file.Paths

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class IPythonContextImpTest {
    lateinit var pm : IPythonContextImp
    lateinit var ipythonConfig: IPythonConfig
    lateinit var zContext: ZContext
    @BeforeEach
    fun beforeEach(){
        this.zContext = ZContext()
        ipythonConfig = TestResource.ipythonConfigForTest()
        pm = IPythonContextImp(ipythonConfig,this.zContext)
    }

    @AfterEach
    fun afterAll(){
        if(pm.getIPythonProcess().get()!=null){
            pm.stopIPython()
        }
    }

    @Test
    fun testStartAndStopListeners(){
        var start = false
        pm.setOnStartProcessListener {
            start = true
        }
        pm.startIPython()
        assertTrue(start)
        var afterStop = false
        var beforeStop = false
        pm.setOnAfterProcessStopListener {
            afterStop = true
        }
        pm.setOnBeforeProcessStopListener{
            beforeStop = true
        }
        pm.stopIPython()
        assertTrue(afterStop)
        assertTrue(beforeStop)
    }

    @Test
    fun startIPython_FromNotStartedYet() {
        assertTrue(pm.getIPythonProcess() is Err)
        val rs = pm.startIPython()
        assertTrue(rs is Ok)
        assertTrue(pm.getIPythonProcess() is Ok)
        assertTrue(pm.getIPythonProcess().get()?.isAlive ?: false)
        assertTrue(pm.getConnectionFileContent() is Ok)
        assertTrue(pm.getChannelProvider() is Ok)
        assertTrue(pm.getSession() is Ok)
        assertTrue(pm.getMsgEncoder() is Ok)
        assertTrue(pm.getMsgIdGenerator() is Ok)
        assertTrue(pm.getHeartBeatService() is Ok)
        assertTrue(pm.getHeartBeatService().unwrap().isServiceRunning())
        assertTrue(Files.exists(Paths.get(ipythonConfig.connectionFilePath)))
    }

    @Test
    fun startIPython_FromAlreadyStarted() {
        val rs0 = pm.startIPython()
        assertTrue(rs0 is Ok)
        val rs = pm.startIPython()
        assertTrue(rs is Ok)
    }

    @Test
    fun stopIPython() {
        pm.startIPython()
        val rs = pm.stopIPython()
        assertTrue(rs is Ok)
        assertTrue(pm.getIPythonProcess() is Err)
        assertFalse(pm.getIPythonProcess().get()?.isAlive ?: false)
        assertTrue(pm.getConnectionFileContent() is Err)
        assertTrue(pm.getSession() is Err)
        assertTrue(pm.getChannelProvider() is Err)
        assertTrue(pm.getMsgEncoder() is Err)
        assertTrue(pm.getMsgIdGenerator() is Err)
        assertTrue(pm.getHeartBeatService() is Err)
        assertFalse(Files.exists(Paths.get(ipythonConfig.connectionFilePath)))
    }
    @Test
    fun stopIPython_onAlreadyStopped() {
        pm.startIPython()
        val rs = pm.stopIPython()
        assertTrue(rs is Ok)
        val rs2 = pm.stopIPython()
        assertTrue(rs2 is Ok)
    }

    @Test
    fun restartIPython() {
        pm.startIPython()
        val oldConnectionFile = pm.getConnectionFileContent().get()
        assertNotNull(oldConnectionFile)
        val rs = pm.restartIPython()
        val newConnectionFile = pm.getConnectionFileContent().get()
        assertTrue(rs is Ok)
        assertNotNull(newConnectionFile)
        assertNotEquals(oldConnectionFile,newConnectionFile)
    }

    @Test
    fun restartIPython_OnStopped() {
        pm.startIPython()
        pm.stopIPython()
        val rs = pm.restartIPython()
        assertTrue(rs is Err)
        assertTrue(rs.getError() is IllegalStateException)
    }
}
