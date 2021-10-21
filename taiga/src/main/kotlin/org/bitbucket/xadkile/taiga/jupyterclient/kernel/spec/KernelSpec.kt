package org.bitbucket.xadkile.taiga.jupyterclient.kernel.spec

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.nio.file.Files
import java.nio.file.Path


data class KernelSpec(
    public val argv: List<String>,
    @SerializedName("display_name")
    public val displayName: String,
    public val language: String,
    @SerializedName("interrput_mode")
    public val interruptMode: InterruptMode,
    public val env: Map<String, String>,
    public val metadata: Map<String, Any>,
    @Transient
    public val resourceDir: Path?
) {
    companion object {
        fun fromResourceDir(resourceDir: Path):KernelSpec{
            val kernelFile = resourceDir.resolve("kernel.json")
            val kernelFileContent = Files.readString(kernelFile,Charsets.UTF_8)
            val gson = Gson()
            val rt = gson.fromJson(kernelFileContent, KernelSpec::class.java).withResourceDir(resourceDir)
            return rt
        }
    }

    fun withResourceDir(resourceDir: Path):KernelSpec{
        return this.copy(resourceDir = resourceDir)
    }
}
