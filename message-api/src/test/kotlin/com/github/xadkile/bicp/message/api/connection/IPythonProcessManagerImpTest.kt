package com.github.xadkile.bicp.message.api.connection

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getError
import com.github.xadkile.bicp.test.utils.JupyterTestConfig
import org.junit.jupiter.api.*

import org.junit.jupiter.api.Assertions.*
import java.nio.file.Files
import java.nio.file.Paths

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class IPythonProcessManagerImpTest {
    lateinit var pm :IPythonProcessManagerImp
    lateinit var ipythonConfig:IPythonConfig
    @BeforeEach
    fun beforeEach(){
        ipythonConfig = JupyterTestConfig.fromFile().toAppConfig()
        pm = IPythonProcessManagerImp(ipythonConfig)
    }

    @AfterEach
    fun afterAll(){
        if(pm.getIPythonProcess()!=null){
            pm.stopIPython()
        }
    }

    @Test
    fun startIPython_FromNotStartedYet() {
        assertNull(pm.getIPythonProcess())
        val rs = pm.startIPython()
        assertTrue(rs is Ok)
        assertNotNull(pm.getIPythonProcess())
        assertNotNull(pm.getConnectionFileContent())
        assertTrue(pm.getIPythonProcess()?.isAlive ?: false)
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
        assertNull(pm.getIPythonProcess())
        assertNull(pm.getConnectionFileContent())
        assertFalse(pm.getIPythonProcess()?.isAlive ?: false)
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
        val oldConnectionFile = pm.getConnectionFileContent()
        assertNotNull(oldConnectionFile)
        val rs = pm.restartIPython()
        val newConnectionFile = pm.getConnectionFileContent()
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
