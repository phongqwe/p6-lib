package com.github.xadkile.bicp.message.api.protocol

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * TODO test this
 * An class to store connection file content
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
        fun fromJsonFile(jsonFilePath: Path):Result<KernelConnectionFileContent,IOException>{
            try{
                val fileContent = Files.readString(jsonFilePath)
                val gson = Gson()
                val rt = gson.fromJson(fileContent,KernelConnectionFileContent::class.java)
                return Ok(rt)
            }catch(e:IOException){
                return Err(e)
            }

        }
        fun fromJsonFile(filePath:String):Result<KernelConnectionFileContent,IOException>{
            return fromJsonFile(Paths.get(filePath))
        }
    }

    fun createShellChannel(): com.github.xadkile.bicp.message.api.channel.ChannelInfo {
        return com.github.xadkile.bicp.message.api.channel.ChannelInfo(
            protocol = this.protocol,
            name = "Shell",
            ipAddress = this.ip,
            port = this.shellPort
        )
    }

    fun createControlChannel(): com.github.xadkile.bicp.message.api.channel.ChannelInfo {
        return com.github.xadkile.bicp.message.api.channel.ChannelInfo.tcp.copy(
            protocol=this.protocol,
            name = "Control",
            ipAddress = this.ip,
            port = this.controlPort
        )
    }

    fun createIOPubChannel(): com.github.xadkile.bicp.message.api.channel.ChannelInfo {
        return com.github.xadkile.bicp.message.api.channel.ChannelInfo.tcp.copy(
            protocol=this.protocol,
            name = "IOPub",
            ipAddress = this.ip,
            port = this.iopubPort
        )
    }

    fun createStdInChannel(): com.github.xadkile.bicp.message.api.channel.ChannelInfo {
        return com.github.xadkile.bicp.message.api.channel.ChannelInfo.tcp.copy(
            protocol=this.protocol,
            name = "StdIn",
            ipAddress = this.ip,
            port = this.stdinPort
        )
    }

    fun createHeartBeatChannel(): com.github.xadkile.bicp.message.api.channel.ChannelInfo {
        return com.github.xadkile.bicp.message.api.channel.ChannelInfo.tcp.copy(
            protocol=this.protocol,
            name = "HeartBeat",
            ipAddress = this.ip,
            port = this.heartBeatPort
        )
    }
}

