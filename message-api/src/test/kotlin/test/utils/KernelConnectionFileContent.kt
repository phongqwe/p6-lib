package test.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
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
    }
}

