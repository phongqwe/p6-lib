package org.bitbucket.xadkile.isp.ide.jupyter.message.api.connection

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.other.ProtocolUtils
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

data class IPythonConfig(val launchCmd:List<String>,val connectionFilePath:String) {

    companion object {
        fun fromFile(filePath: Path):Result<IPythonConfig,IOException>{
            try{
                val fileContent = Files.readString(filePath)
                val rt:IPythonConfig = ProtocolUtils.msgGson.fromJson(fileContent,IPythonConfig::class.java)
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
