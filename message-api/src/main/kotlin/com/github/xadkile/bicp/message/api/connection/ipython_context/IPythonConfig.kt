package com.github.xadkile.bicp.message.api.connection.ipython_context

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.protocol.ProtocolUtils
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

data class IPythonConfig internal constructor(val launchCmd:List<String>,
                         val connectionFilePath:String,
                         val milliSecStartTime:Long,
                         val milliSecStopTime:Long
) {

    companion object {
        fun fromFile(filePath: Path):Result<IPythonConfig,IOException>{
            try{
                val fileContent = Files.readString(filePath)
                val rt: IPythonConfig = ProtocolUtils.msgGson.fromJson(fileContent,
                    IPythonConfig::class.java)
                return Ok(rt)
            }catch (e:IOException){
                return Err(e)
            }
        }
    }

    fun makeLaunchCmmd():List<String> {
        return (launchCmd + connectionFilePath)
    }
}
