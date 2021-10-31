package org.bitbucket.xadkile.myide.ide.jupyterclient.kernel.spec

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.nio.file.Files
import java.nio.file.Path

/**
 * A [KernelSpec] represents the content of of a kernel.json
 * It contains information needed to launch a kernel such as:
 *  - command line executable, args
 *  - misc info: such as kernel name
 */
data class KernelSpec(
    val argv: List<String> = emptyList(),
    @SerializedName("display_name")
    val displayName: String ="",
    val language: String ="",
    @SerializedName("interrput_mode")
    val interruptMode: InterruptMode = InterruptMode.NOT_YET,
    val env: Map<String, String> = emptyMap(),
    // custom metadata info, may vary for different kernel
    val metadata: Any? = null,
    // resourceDir = kernel folder
    @Transient
    val resourceDir: Path? = null,
    @Transient
    val info:KernelSpecInfo? = null
) {
    companion object {
        /**
         * read kernel.json in [kernelDir] to construct A KernelSpec
         * [kernelDir] path to kernel folder that holds a "kernel.json"
         */
        fun fromKernelDir(kernelDir: Path): KernelSpec {
            val kernelJson = kernelDir.resolve("kernel.json")
            return fromKernelJson(kernelJson)
        }
        /**
         * read a kernel.json to construct A KernelSpec
         * [kernelJson] a "kernel.json" file that describe instruction on how to start a kernel
         */
        fun fromKernelJson(kernelJson:Path):KernelSpec{
            val kernelFileContent = Files.readString(kernelJson, Charsets.UTF_8)
            val gson = Gson()
            val rt = gson.fromJson(kernelFileContent, KernelSpec::class.java).withResourceDir(kernelJson.parent)
            return rt
        }
    }

    fun withResourceDir(resourceDir: Path): KernelSpec {
        return this.copy(resourceDir = resourceDir)
    }
    fun withInfo(info:KernelSpecInfo):KernelSpec{
        return this.copy(info = info)
    }
}
