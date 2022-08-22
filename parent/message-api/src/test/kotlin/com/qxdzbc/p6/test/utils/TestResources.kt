package com.qxdzbc.p6.test.utils

import com.qxdzbc.p6.message.api.connection.kernel_context.KernelConfig
import com.qxdzbc.p6.message.api.connection.kernel_context.KernelConfigImp
import com.google.gson.Gson
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths

class TestResources {
    companion object{
        fun kernelConfigForTest(): KernelConfig {
            val resource: URL? = javaClass.classLoader.getResource("jupyterConfig.json")
            if (resource == null) {
                throw Exception("missing jupyterConfig.json")
            } else {
                val fileContent = Files.readString(Paths.get(resource.toURI()))
                val o = Gson().fromJson(fileContent, KernelConfigImp::class.java)
                return o
            }
        }
        fun dummyProcessCmd(count:Int) = listOf("java","-jar","dummy_process.jar",count.toString())
    }
}
