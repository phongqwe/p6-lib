package org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.channel.ChannelInfo
import java.net.ServerSocket
import java.nio.file.Files
import java.nio.file.Path

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
    val heartBeatPort:Int,
    val ip:String,
    val key:String,
    @SerializedName("transport")
    val protocol:String,
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
    }

    fun createShellChannel(): ChannelInfo {
        return ChannelInfo(
            protocol=this.protocol,
            name = "Shell",
            ipAddress = this.ip,
            port = this.shellPort
        )
    }

    fun createControlChannel(): ChannelInfo {
        return ChannelInfo.tcp.copy(
            protocol=this.protocol,
            name = "Control",
            ipAddress = this.ip,
            port = this.controlPort
        )
    }

    fun createIOPubChannel(): ChannelInfo {
        return ChannelInfo.tcp.copy(
            protocol=this.protocol,
            name = "IOPub",
            ipAddress = this.ip,
            port = this.iopubPort
        )
    }

    fun createStdInChannel(): ChannelInfo {
        return ChannelInfo.tcp.copy(
            protocol=this.protocol,
            name = "StdIn",
            ipAddress = this.ip,
            port = this.stdinPort
        )
    }

    fun createHeartBeatChannel():ChannelInfo{
        return ChannelInfo.tcp.copy(
            protocol=this.protocol,
            name = "HeartBeat",
            ipAddress = this.ip,
            port = this.heartBeatPort
        )
    }
}

