package com.github.xadkile.bicp.ide.jupyterclient.kernel.spec

import java.nio.file.Files
import java.nio.file.Path

/**
 * [specDir] dir to kernel folder
 * [kernelName] name of a kernel
 */
data class KernelSpecInfo(val kernelName:String, val specDir: Path){

    /**
     * get path of kernel.json file in [specDir]
     */
    @Throws(IllegalStateException::class)
    fun getKernelJsonPath():Path{
        val rt= specDir.resolve("kernel.json").toAbsolutePath()
        if(Files.exists(rt)){
            return rt
        }else{
            throw IllegalStateException("kernel.json does not exist")
        }
    }
}
