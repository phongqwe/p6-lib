package org.bitbucket.xadkile.taiga

import org.bitbucket.xadkile.taiga.jupyterclient.kernel.KernelManager
import org.bitbucket.xadkile.taiga.jupyterclient.kernel.KernelManagerImp
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Paths
import java.util.concurrent.Executors
import java.util.function.Consumer

internal class StreamGobbler(private val inputStream: InputStream, private val consumer: Consumer<String>) :
    Runnable {
    override fun run() {
        BufferedReader(InputStreamReader(inputStream)).lines()
            .forEach(consumer)
    }
}


//['/home/abc/Applications/anaconda3/envs/dl_hw_01/bin/python', '-m', 'ipykernel_launcher', '-f', '/tmp/tmpwmp8kgkt.json']

fun main(){
    KernelManagerImp().startKernel()
    // -m : run library module as script
    // ipykernel_launcher: launch kernel

    // /tmp/tmpwmp8kgkt.json : connection file
    // TODO: python -m ipykernel_launcher --help: this for the full list of arg for kernel, including ports and shit
    // TODO: detect python executable
    // TODO: todo generate connection file name
    // TODO: generate port, pass the port into the command line
    // -f : option of ipykernel_launcher, location for connection file
    // --ip: for host
    // --hb: heart beat port
    // --shell: shell port
    // --iopub: iopub port
    // --stdin: stdin port
    // --control: control port
    // --transport: tcp or ipc (pick tcp)
    // --secure: signature scheme something, they use this: hmac-sha256
    // kernel_name does not seem to be important..., always empty string



}

fun processBuilder(){
    val envBinDir = "/home/abc/Applications/anaconda3/envs/dl_hw_01/bin"
    val builder = ProcessBuilder()
    val l = listOf("/home/abc/Applications/anaconda3/envs/dl_hw_01/bin/python", "-m", "ipykernel_launcher", "-f", "/tmp/tmpwmp8kgkt.json")
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

data class MessageHeader(
    val msg_id:String="",
    val session:String="",
    val username:String="",
    val data:String="", //ISO 8601
    val msg_type:String="",
    val version:String="5.0"
)

data class Request(
    val header:MessageHeader= MessageHeader(),
    val parent_header:MessageHeader?=null,
    val metadata: Any = object{},
    val content:Any=object{},
    val buffers:List<Any> = emptyList()
)
