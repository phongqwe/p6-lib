package org.bitbucket.xadkile.isp.ide.jupyterclient.kernel.spec

import org.bitbucket.xadkile.isp.ide.jupyterclient.path.JPDirFinder
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.regex.Pattern
import java.util.stream.Collectors

/**
 *  [kernelParentDirList] is a directory that contains kernel folders
 *  [kernelParentDirList] is extracted from data directories
 */
class KernelSpecManager(private val kernelParentDirList: List<Path>) {

    companion object {
        val KERNELS = "kernels"
        fun fromDataDirs(dataPath: List<Path>, ipythonDir: Path): KernelSpecManager {
            val kernelDirs = dataPath.map {
                it.resolve(KERNELS)
            }.filter { Files.exists(it) } + ipythonDir.resolve(KERNELS).toAbsolutePath()
            return KernelSpecManager(kernelDirs)
        }

        /**
         * create [KernelSpecManager] from a [JPDirFinder]
         */
        fun fromDirFinder(dirFinder: JPDirFinder): KernelSpecManager {
            return KernelSpecManager.fromDataDirs(dirFinder.findDataPath(), dirFinder.findIPythonDir())
        }
    }

    private fun checkKernelName(kernelName: String): Boolean {
        val validNamePattern = Pattern.compile("^[-a-z0-9._]+$")
        val lowerCaseName = kernelName.toLowerCase()
        val rt = validNamePattern.matcher(lowerCaseName).matches()
        return rt
    }

    /**
     * @return true if [dir] contains a kernel.json file, false otherwise
     */
    private fun isKernelDir(dir: Path): Boolean {
        return Files.isDirectory(dir) && Files.isRegularFile(dir.resolve("kernel.json"))
    }

    /**
     * Find the spec directory (contain kernel.json) for a kernel with [kernelName]
     * return the first ok
     */
    private fun findSpecDirectory(kernelName: String): Path? {
        for (kernelParentDir in this.kernelParentDirList) {
            try {
                val dirContentList = Files.list(kernelParentDir).collect(Collectors.toList())
                for (kernelDir in dirContentList) {
                    if ((kernelDir.fileName.toString() == kernelName) && isKernelDir(kernelDir)) {
                        return kernelDir
                    }
                }
            } catch (e: IOException) {
                println("cannot get content of dir: $kernelParentDir")
                println("skip")
                continue
            }
        }
        return null
    }

    fun getKernelSpecByName(kernelName: String): KernelSpec {
        if (false == checkKernelName(kernelName)) {
            println("Kernel name is invalid: $kernelName")
        }
        val resourceDir = this.findSpecDirectory(kernelName)
            ?: throw IllegalStateException("No such kernel: $kernelName")
        return KernelSpec
            .fromKernelDir(resourceDir)
            .withInfo(KernelSpecInfo(kernelName,resourceDir))
    }

    fun getAllKernelSpec(): List<KernelSpec> {
        val rt = mutableListOf<KernelSpec>()
        for (kernelParentDir in this.kernelParentDirList) {
            try {
                val dirContentList = Files.list(kernelParentDir).collect(Collectors.toList())
                for (kernelDir in dirContentList) {
                    if (isKernelDir(kernelDir)) {
                        val kernelSpecInfo = KernelSpecInfo(kernelDir.fileName.toString(), kernelDir)
                        val kernelSpec = KernelSpec
                            .fromKernelDir(kernelDir)
                            .withInfo(kernelSpecInfo)
                        rt.add(kernelSpec)
                    }
                }
            } catch (e: IOException) {
                println("cannot get content of dir: $kernelParentDir")
                println("skip")
                continue
            }
        }
        return rt
    }
}
