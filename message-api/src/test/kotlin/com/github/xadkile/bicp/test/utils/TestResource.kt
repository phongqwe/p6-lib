package com.github.xadkile.bicp.test.utils

import com.github.xadkile.bicp.message.api.connection.IPythonConfig
import com.google.gson.Gson
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths

class TestResource {
    companion object{
        fun ipythonConfigForTest(): IPythonConfig {
            val resource: URL? = javaClass.classLoader.getResource("jupyterConfig.json")
            if (resource == null) {
                throw Exception("missing jupyterConfig.json")
            } else {
                val fileCOntent = Files.readString(Paths.get(resource.toURI()))
                val o = Gson().fromJson(fileCOntent, IPythonConfig::class.java)
                return o
            }
        }
    }
}
