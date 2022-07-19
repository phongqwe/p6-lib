package com.emeraldblast.p6.message.api.connection.kernel_context

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.get
import com.emeraldblast.p6.message.api.connection.kernel_context.errors.KernelErrors
import com.emeraldblast.p6.message.api.connection.kernel_services.KernelServiceManager
import com.emeraldblast.p6.message.api.message.protocol.KernelConnectionFileContent
import com.emeraldblast.p6.message.di.DaggerMessageApiComponent
import com.emeraldblast.p6.test.utils.TestResources
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
    lateinit var kc: KernelContext
    lateinit var kernelConfig: KernelConfig
    lateinit var zContext: ZContext
    lateinit var ksm: KernelServiceManager
    val cfLoc = "/tmp/kernel4.json"
    val cfLoc2 = "/tmp/kernel5.json"
    val l1 = listOf("/home/abc/Applications/anaconda3/envs/p6/bin/python", "-m", "ipykernel_launcher", "-f")
    val l2 = listOf("/home/abc/Applications/anaconda3/envs/p7/bin/python", "-m", "ipykernel_launcher", "-f")
    val config1 = KernelConfigImp(
        launchCmd = l1,
        connectionFilePath = cfLoc
    )
    val config2 = KernelConfigImp(
        launchCmd = l1,
        connectionFilePath = cfLoc2
    )
    val mockConnectionFileContent = KernelConnectionFileContent(
        shellPort = 1,
        iopubPort = 2,
        stdinPort = 3,
        controlPort = 3,
        heartBeatPort = 3,
        ip = "ip",
        key = "123",
        protocol = "qwe",
        signatureScheme = "q",
        kernelName = "kn"
    )

    @BeforeEach
    fun beforeEach() {
        this.zContext = ZContext()
        kernelConfig = TestResources.kernelConfigForTest()
        val dcomponent = DaggerMessageApiComponent.builder()
            .kernelConfig(kernelConfig)
            .kernelCoroutineScope(GlobalScope)
            .networkServiceCoroutineDispatcher(Dispatchers.IO)
            .build()
        kc = dcomponent.kernelContext()
        ksm = dcomponent.kernelServiceManager()
    }

    @AfterEach
    fun afterAll() {
        runBlocking {
            kc.stopAll()
        }
    }

    @Test
    fun testKernelOnOff() {
        runBlocking {

            val cmp = DaggerMessageApiComponent.builder()
                .kernelConfig(null)
                .kernelCoroutineScope(GlobalScope)
                .networkServiceCoroutineDispatcher(Dispatchers.IO)
                .build()

            val kernel = cmp.kernelContext()
            val msgMan = cmp.kernelServiceManager()

            println("CONFIG 1")
            kernel.setKernelConfig(config1)
            assertTrue(kernel.startAll() is Ok)
            assertTrue(msgMan.startAll() is Ok)

            delay(2000)

            assertTrue(kernel.stopAll() is Ok)
            assertTrue(msgMan.stopAll() is Ok)

            println("CONFIG 2")
            kernel.setKernelConfig(config2)
            assertTrue(kernel.startAll() is Ok)
            assertTrue(msgMan.startAll() is Ok)
            delay(3000)
            assertTrue(kernel.stopAll() is Ok)
            assertTrue(msgMan.stopAll() is Ok)
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
    fun startKernel_FromNotStartedYet() = runBlocking {
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
        assertTrue(Files.exists(Paths.get(kernelConfig.connectionFilePath)))
    }

    @Test
    fun `startKernel with kernel config`(){
        assertTrue { kc.isKernelNotRunning() }
        val rs = kc.startKernel(kernelConfig)
        assertTrue { rs is Ok }
        assertTrue { kc.isKernelRunning() }
    }

    @Test
    fun `startKernel with connect file content`(){
        assertTrue { kc.isKernelNotRunning() }
        val rs = kc.startKernel(mockConnectionFileContent)
        assertTrue { rs is Ok }
        assertTrue { kc.isKernelRunning() }
    }

    @Test
    fun `startKernel with connect file path`(){
        assertTrue { kc.isKernelNotRunning() }
        val path = Paths.get(javaClass.classLoader.getResource("kernel5.json").toURI()!!)
        val rs = kc.startKernel(path)
        assertTrue { rs is Ok }
        assertTrue { kc.isKernelRunning() }
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
        assertFalse(Files.exists(Paths.get(kernelConfig.connectionFilePath)))
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
    fun restartKernel() = runBlocking {
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
    fun `restartKernel with new kernel config`() {
        val startRs = kc.startAll()
        assertTrue { startRs is Ok }
        val restartRs = kc.restartKernel(config2)
        assertTrue { restartRs is Ok }
        assertTrue { kc.isKernelRunning() }
    }

    @Test
    fun `restartKernel with new connection file content`() {
        val startRs = kc.startAll()
        assertTrue { startRs is Ok }

        val restartRs = kc.restartKernel(mockConnectionFileContent)
        assertTrue { restartRs is Ok }
        assertTrue { kc.isKernelRunning() }
    }

    @Test
    fun `restartKernel with invalid connection file path`() {
        val startRs = kc.startAll()
        assertTrue { startRs is Ok }
        val invalidPath = Paths.get("qwe")
        val restartRs = kc.restartKernel(invalidPath)
        println(restartRs)
        assertTrue { restartRs is Err }
        assertTrue { kc.isKernelNotRunning() }
    }
    @Test
    fun `restartKernel with valid connection file path`() {
        val startRs = kc.startAll()
        assertTrue { startRs is Ok }
        val validConnectionFilePath = Paths.get(javaClass.classLoader.getResource("kernel5.json").toURI()!!)
        val restartRs2 = kc.restartKernel(validConnectionFilePath)
        assertTrue { restartRs2 is Ok }
        assertTrue { kc.isKernelRunning() }
    }

    @Test
    fun restartIPython_OnStopped() = runBlocking {
        kc.startAll()
        runBlocking {
            kc.stopAll()
            val rs = kc.restartKernel()
            assertTrue(rs is Err)
            assertTrue(rs.error.isType(KernelErrors.KernelContextIllegalState.header))
        }
    }
}
