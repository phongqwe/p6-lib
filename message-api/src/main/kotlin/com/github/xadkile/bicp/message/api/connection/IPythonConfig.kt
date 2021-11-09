package com.github.xadkile.bicp.message.api.connection

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.protocol.other.ProtocolUtils
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

data class IPythonConfig(val launchCmd:List<String>,val connectionFilePath:String) {

    companion object {
        fun fromFile(filePath: Path):Result<com.github.xadkile.bicp.message.api.connection.IPythonConfig,IOException>{
            try{
                val fileContent = Files.readString(filePath)
                val rt: com.github.xadkile.bicp.message.api.connection.IPythonConfig = ProtocolUtils.msgGson.fromJson(fileContent,
                    com.github.xadkile.bicp.message.api.connection.IPythonConfig::class.java)
                return Ok(rt)
            }catch (e:IOException){
                return Err(e)
            }
        }
    }

    fun makeLaunchCmmd():String {
        return (launchCmd + connectionFilePath).joinToString(" ")
    }
}
