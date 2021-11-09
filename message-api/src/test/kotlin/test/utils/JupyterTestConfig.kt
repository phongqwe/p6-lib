package test.utils

import com.github.michaelbull.result.get
import com.github.michaelbull.result.unwrap
import com.google.gson.Gson
import com.github.xadkile.bicp.message.api.protocol.KernelConnectionFileContent
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths


class JupyterTestConfig(private val launchCmd: List<String> = emptyList(), private val connectFilePath: String = "") {
    fun makeCmd(): List<String> = launchCmd + connectFilePath
    fun connectionFile() = KernelConnectionFileContent.fromJsonFile(Paths.get(this.connectFilePath)).unwrap()

    companion object {
        fun fromFile(): JupyterTestConfig {
            val resource: URL? = javaClass.classLoader.getResource("jupyterConfig.json")
            if (resource == null) {
                throw Exception("missing jupyterConfig.json")
            } else {
                val fileCOntent = Files.readString(Paths.get(resource.toURI()))
                val o = Gson().fromJson(fileCOntent, JupyterTestConfig::class.java)
                return o
            }
        }
    }
}
