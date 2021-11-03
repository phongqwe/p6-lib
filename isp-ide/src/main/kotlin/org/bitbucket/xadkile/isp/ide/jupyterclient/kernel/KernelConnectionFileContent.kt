package org.bitbucket.xadkile.isp.ide.jupyterclient.kernel

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import java.net.ServerSocket
import java.nio.file.Files
import java.nio.file.Path

/**
 * like a connection file
 *   "shell_port": 38553,
 *   "iopub_port": 37649,
 *   "stdin_port": 38171,
 *   "control_port": 37545,
 *   "hb_port": 55453,
 *   "ip": "127.0.0.1",
 *   "key": "7d7e5baa-64b13932634fe3ca0a4cbbb2",
 *   "transport": "tcp",
 *   "signature_scheme": "hmac-sha256",
 *   "kernel_name": ""
 */
data class KernelConnectionFileContent(
    @SerializedName("shell_port")
    val shellPort:Int,
    @SerializedName("iopub_port")
    val iopubPort:Int,
    @SerializedName("stdin_port")
    val stdinPort:Int,
    @SerializedName("control_port")
    val controlPort:Int,
    @SerializedName("hb_port")
    val hbPort:Int,
    val ip:String,
    val key:String,
    val transport:String,
    @SerializedName("signature_scheme")
    val signatureScheme:String,
    @SerializedName("kernel_name")
    val kernelName:String
){
    companion object CO{
        fun fromJsonFile(jsonFilePath: Path):KernelConnectionFileContent{
            val fileContent = Files.readString(jsonFilePath)
            val gson = Gson()
            val rt = gson.fromJson(fileContent,KernelConnectionFileContent::class.java)
            return rt
        }

        fun makeSHA256LocalOne(key:String):KernelConnectionFileContent{
            return makeOne("127.0.0.1","hmac-sha256",key)
        }
        fun makeOne(ip:String,signatureScheme: String,key:String):KernelConnectionFileContent{
            val ports = findFiveOpenPorts()
            return KernelConnectionFileContent(
                shellPort = ports[0],
                iopubPort = ports[1],
                stdinPort = ports[2],
                controlPort = ports[3],
                hbPort = ports[4],
                ip = ip,
                key = key,
                transport = "tcp",
                signatureScheme = signatureScheme,
                kernelName = ""
            )
        }
        private fun findFiveOpenPorts(): List<Int> {
            val ports:List<Int> = (0 until 5).map{
                // port = 0 => system will automatically find a random port for me
                val socket = ServerSocket(0).also{
                    it.reuseAddress=true
                }
                val port:Int = socket.localPort
                check(port !=0){"Could not find a port for port at $it"}
                port
            }
            return ports
        }
    }

    fun withKey(key:String):KernelConnectionFileContent{
        return this.copy(key=key)
    }

    fun toJson():String{
        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(this)
    }
}
