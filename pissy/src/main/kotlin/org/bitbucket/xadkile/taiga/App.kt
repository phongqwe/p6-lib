package org.bitbucket.xadkile.taiga

import org.bitbucket.xadkile.taiga.jupyterclient.client.message.MessageHeaderImp
import org.bitbucket.xadkile.taiga.jupyterclient.client.message.Request
import org.bitbucket.xadkile.taiga.jupyterclient.client.message.message.ShutdownRequestContent
import org.bitbucket.xadkile.taiga.jupyterclient.kernel.KernelConnectionFileContent
import org.zeromq.SocketType
import org.zeromq.ZContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.Executors
import java.util.function.Consumer

internal class StreamGobbler(private val inputStream: InputStream, private val consumer: Consumer<String>) :
    Runnable {
    override fun run() {
        BufferedReader(InputStreamReader(inputStream)).lines()
            .forEach(consumer)
    }
}

/**
 * run a piece of python code to start a kernel
 * [pythonExePath] should be obtain externally (from UI or something)
 * connection info can be extract from stdio
 * DONT PORT THIS TO JAVA, IT'S STUPID, JUST USE JUPYTER CLIENT
 */
fun startKernel(pythonExePath: String): Process {
    val code = "import jupyter_client\n" +
            "kernelMan =jupyter_client.KernelManager()\n" +
            "kernelMan.start_kernel()"
    val processBuilder = ProcessBuilder(pythonExePath, "-c", code)
    val rt = processBuilder.inheritIO().start()
    return rt
}

/**
 * TODO write kill kernel function
 */
fun main() {
    ZContext().use { context ->
        val session = "3109c299-284bfe3e2419cacda2179ba7"

        val messId = UUID.randomUUID().toString()
        val connectionFile = KernelConnectionFileContent.fromJsonFile(Paths.get("/tmp/phong-kernel4.json"))
        val key = connectionFile.key
        val request = Request(
            header = MessageHeaderImp.convenientCreate(
                sessionId = session,
                username = "abc",
                msgType = ShutdownRequestContent.NoRestart.getMsgType(),
            ),
            parent_header = null,
            content = ShutdownRequestContent.NoRestart,
            metadata = null
        )
        val payloadList: List<ByteArray> = request.makePayload(key)

        val socket = context.createSocket(SocketType.REQ)
        val port = connectionFile.controlPort
        socket.connect("tcp://127.0.0.1:$port")

        socket.sendMore(payloadList[0]) // <IDS|MSG>
        socket.sendMore(payloadList[1]) // hmac sig
        socket.sendMore(payloadList[2]) // header
        socket.sendMore(payloadList[3]) // parent header
        socket.sendMore(payloadList[4]) // parent_header
        socket.send(payloadList[5]) // content
        val rt = String(socket.recv()).toString()
        println(rt)
    }
}

fun processBuilder() {
    val envBinDir = "/home/abc/Applications/anaconda3/envs/dl_hw_01/bin"
    val builder = ProcessBuilder()
    val l = listOf(
        "/home/abc/Applications/anaconda3/envs/dl_hw_01/bin/python",
        "-m",
        "ipykernel_launcher",
        "-f",
        "/tmp/tmpwmp8kgkt.json"
    )
    builder.command(l)
    builder.redirectErrorStream(true)
    builder.directory(File(System.getProperty("user.home")))
    val process = builder.start()
    val streamGobbler: StreamGobbler = StreamGobbler(process.inputStream,
        Consumer { x: String? -> println(x) })
    Executors.newSingleThreadExecutor().submit(streamGobbler)
    val exitCode = process.waitFor()
    assert(exitCode == 0)
}



