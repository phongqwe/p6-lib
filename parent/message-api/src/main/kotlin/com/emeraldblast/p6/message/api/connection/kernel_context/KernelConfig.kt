package com.emeraldblast.p6.message.api.connection.kernel_context

import com.emeraldblast.p6.common.exception.error.ErrorReport
import com.emeraldblast.p6.message.api.connection.kernel_context.errors.KernelErrors
import com.emeraldblast.p6.message.api.message.protocol.KernelConnectionFileContent
import com.emeraldblast.p6.message.api.message.protocol.ProtocolUtils
import com.github.michaelbull.result.*
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

interface KernelConfig {
    val launchCmd: List<String>
    val connectionFilePath: String
    val timeOut: KernelTimeOut
    val kernelConnectionFileContent: KernelConnectionFileContent?
    fun makeCompleteLaunchCmd(): List<String>
}

/**
 * Information to start a kernel process
 * [launchCmd] a command line to start kernel process, not including connection file path
 * [connectionFilePath] path to connection file
 *
 */
data class KernelConfigImp constructor(
    override val launchCmd: List<String> = emptyList(),
    override val connectionFilePath: String = "",
    override val timeOut: KernelTimeOut = KernelTimeOut()
) :KernelConfig{

    companion object {
        fun fromFile(filePath: Path): Result<KernelConfig, ErrorReport> {
            try {
                val fileContent = Files.readString(filePath)
                val rt: KernelConfig = ProtocolUtils.msgGson.fromJson(
                    fileContent,
                    KernelConfigImp::class.java
                )
                return Ok(rt)
            } catch (e: IOException) {
                return Err(
                    ErrorReport(
                        header = KernelErrors.CantCreateKernelConfig.header,
                        data = KernelErrors.CantCreateKernelConfig.Data(e),
                    )
                )
            }
        }
    }

    override val kernelConnectionFileContent: KernelConnectionFileContent?
        get() {
            return KernelConnectionFileContent.fromJsonFile2(this.connectionFilePath).getOr(null)
        }

    override fun makeCompleteLaunchCmd(): List<String> {
        return (launchCmd + connectionFilePath)
    }

}
