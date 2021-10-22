package org.bitbucket.xadkile.taiga.jupyterclient.kernel.spec

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.nio.file.Files
import java.nio.file.Path


data class KernelSpec(
    val argv: List<String> = emptyList(),
    @SerializedName("display_name")
    val displayName: String ="",
    val language: String ="",
    @SerializedName("interrput_mode")
    val interruptMode: InterruptMode = InterruptMode.NOT_YET,
    val env: Map<String, String> = emptyMap(),
    val metadata: Map<String, Any> = emptyMap(),
    @Transient
    val resourceDir: Path? = null
) {
    companion object {
        /**
         * [resourceDir] path to kernel folder
         */
        fun fromResourceDir(resourceDir: Path): KernelSpec {
            val kernelFile = resourceDir.resolve("kernel.json")
            val kernelFileContent = Files.readString(kernelFile, Charsets.UTF_8)
            val gson = Gson()
            val rt = gson.fromJson(kernelFileContent, KernelSpec::class.java).withResourceDir(resourceDir)
            return rt
        }
    }

    fun withResourceDir(resourceDir: Path): KernelSpec {
        return this.copy(resourceDir = resourceDir)
    }
}
