package org.bitbucket.xadkile.taiga.jupyterclient.kernel.spec

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.regex.Pattern
import java.util.stream.Collectors

/**
 * @property kernelDirList kernel directories are extracted from data directories
 */
class KernelSpecManager(private val kernelDirList: List<Path>) {

    companion object {
        val KERNELS = "kernels"
        val NATIVE_KERNEL_NAME = "python3"
        fun fromDataDirs(dataDirs: List<Path>, ipythonDir: Path): KernelSpecManager {
            val kernelDirs = dataDirs.map {
                it.resolve(KERNELS)
            } + ipythonDir.resolve(KERNELS).toAbsolutePath()
            return KernelSpecManager(kernelDirs)
        }
    }

    private fun checkKernelName(kernelName: String): Boolean {
        val validNamePattern = Pattern.compile("^[-a-z0-9._]+$")
        val lowerCaseName = kernelName.toLowerCase()
        val rt = validNamePattern.matcher(lowerCaseName).matches()
        return rt
    }

    private fun isKernelDir(dir: Path): Boolean {
        return Files.isDirectory(dir) && Files.isRegularFile(dir.resolve("kernel.json"))
    }

    /**
     * Find the resource directory of a named kernel spec
     * TODO how about native kernel
     */
    private fun findSpecDirectory(kernelName: String): Path? {
        for (kernelDir in this.kernelDirList) {
            try {
                val dirContentList = Files.list(kernelDir).collect(Collectors.toList())
                for (file in dirContentList) {
                    val filePath = kernelDir.resolve(file)
                    if (file.toString().toLowerCase() == kernelName && isKernelDir(filePath)) {
                        return filePath
                    }
                }
            } catch (e: IOException) {
                throw e
            }
        }
        return null
    }

    fun getKernelSpec(kernelName: String): KernelSpec {
        if (checkKernelName(kernelName)) {
            println("Kernel name is invalid: $kernelName")
        }
        val resourceDir = this.findSpecDirectory(kernelName.toLowerCase())
            ?: throw IllegalStateException("No such kernel: ${kernelName}")
        return this.getKernelSpecByName(kernelName,resourceDir)
    }

    private fun getKernelSpecByName(kernelName: String,resourceDir:Path): KernelSpec {
            TODO()
    }
}
