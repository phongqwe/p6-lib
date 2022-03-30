package com.github.xadkile.p6.message.api.message.protocol

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.xadkile.p6.common.exception.error.ErrorReport
import com.github.xadkile.p6.message.api.connection.kernel_context.context_object.ChannelInfo
import com.github.xadkile.p6.message.api.message.protocol.errors.MsgProtocolErrors
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
        fun fromJsonFile2(jsonFilePath: Path):Result<KernelConnectionFileContent, ErrorReport>{
            try{
                val fileContent = Files.readString(jsonFilePath)
                val gson = Gson()
                val rt = gson.fromJson(fileContent,
                    KernelConnectionFileContent::class.java)
                return Ok(rt)
            }catch(e:IOException){
                return Err(
                    ErrorReport(
                        header = MsgProtocolErrors.IOError,
                        data = MsgProtocolErrors.IOError.Data(e)
                    )
                )
            }
        }
        fun fromJsonFile2(filePath:String):Result<KernelConnectionFileContent, ErrorReport>{
            return fromJsonFile2(Paths.get(
                filePath))
        }

    }

    fun createShellChannel(): ChannelInfo {
        return ChannelInfo(
            protocol = this.protocol,
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

    fun createHeartBeatChannel(): ChannelInfo {
        return ChannelInfo.tcp.copy(
            protocol=this.protocol,
            name = "HeartBeat",
            ipAddress = this.ip,
            port = this.heartBeatPort
        )
    }
}

