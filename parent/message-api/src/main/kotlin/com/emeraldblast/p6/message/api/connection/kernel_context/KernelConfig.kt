package com.emeraldblast.p6.message.api.connection.kernel_context

import com.emeraldblast.p6.common.exception.error.ErrorReport
import com.emeraldblast.p6.message.api.connection.kernel_context.errors.KernelErrors
import com.emeraldblast.p6.message.api.message.protocol.KernelConnectionFileContent
import com.emeraldblast.p6.message.api.message.protocol.ProtocolUtils
import com.github.michaelbull.result.*
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/**
 * Information to start a kernel process
 * [launchCmd] a command line to start kernel process, not including connection file path
 * [connectionFilePath] path to connection file
 *
 */
data class KernelConfig constructor(
    private val launchCmd: List<String>,
    private val connectionFilePath: String,
    val timeOut: KernelTimeOut
) {

    companion object {
        fun fromFile(filePath: Path): Result<KernelConfig, ErrorReport> {
            try {
                val fileContent = Files.readString(filePath)
                val rt: KernelConfig = ProtocolUtils.msgGson.fromJson(
                    fileContent,
                    KernelConfig::class.java
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

    val kernelConnectionFileContent: KernelConnectionFileContent?
        get() {
            return KernelConnectionFileContent.fromJsonFile2(this.connectionFilePath).getOr(null)
        }

    fun makeCompleteLaunchCmmd(): List<String> {
        return (launchCmd + connectionFilePath)
    }

    fun getConnectionFilePath(): String {
        return this.connectionFilePath
    }

    fun getLaunchCmd(): List<String> {
        return this.launchCmd
    }
}
