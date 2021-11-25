package com.github.xadkile.bicp.test.utils

import com.github.xadkile.bicp.message.api.connection.ipython_context.KernelConfig
import com.google.gson.Gson
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths

class TestResources {
    companion object{
        fun ipythonConfigForTest(): KernelConfig {
            val resource: URL? = javaClass.classLoader.getResource("jupyterConfig.json")
            if (resource == null) {
                throw Exception("missing jupyterConfig.json")
            } else {
                val fileCOntent = Files.readString(Paths.get(resource.toURI()))
                val o = Gson().fromJson(fileCOntent, KernelConfig::class.java)
                return o
            }
        }

        fun defaultIPythonProcessCmd():List<String> = this.ipythonConfigForTest().makeCompleteLaunchCmmd()
        fun dummyProcessCmd(count:Int) = listOf("java","-jar","dummy_process.jar",count.toString())
    }
}
