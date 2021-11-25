package com.github.xadkile.bicp.message.api.connection.ipython_context

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.msg.protocol.ProtocolUtils
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/**
 * Information to start a kernel process
 * [launchCmd] a command line to start kernel process, not including connection file path
 * [connectionFilePath] path to connection file
 *
 */
data class KernelConfig internal constructor(private val launchCmd:List<String>,
                                             private val connectionFilePath:String,
) {

    companion object {
        fun fromFile(filePath: Path):Result<KernelConfig,IOException>{
            try{
                val fileContent = Files.readString(filePath)
                val rt: KernelConfig = ProtocolUtils.msgGson.fromJson(fileContent,
                    KernelConfig::class.java)
                return Ok(rt)
            }catch (e:IOException){
                return Err(e)
            }
        }
    }

    fun makeCompleteLaunchCmmd():List<String> {
        return (launchCmd + connectionFilePath)
    }

    fun getConnectionFilePath():String{
        return this.connectionFilePath
    }

    fun getLaunchCmd():List<String>{
        return this.launchCmd
    }
}
