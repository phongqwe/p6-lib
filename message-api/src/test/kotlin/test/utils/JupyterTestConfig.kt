package test.utils

import com.google.gson.Gson
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths


class JupyterTestConfig(private val launchCmd:List<String> = emptyList(), private val connectFilePath:String=""){
    fun makeCmd():List<String> = launchCmd + connectFilePath
    fun connectionFile() = KernelConnectionFileContent.fromJsonFile(Paths.get(this.connectFilePath))
    companion object {
        fun fromFile():JupyterTestConfig{
            val resource: URL? = javaClass.classLoader.getResource("jupyterConfig.json")
            if(resource==null){
                throw Exception("missing jupyterConfig.json")
            }else{
                val fileCOntent= Files.readString(Paths.get(resource.toURI()))
                val o = Gson().fromJson(fileCOntent,JupyterTestConfig::class.java)
                return o
            }
        }
    }
}
