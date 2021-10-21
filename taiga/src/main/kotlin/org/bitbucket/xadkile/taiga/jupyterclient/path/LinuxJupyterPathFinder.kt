package org.bitbucket.xadkile.taiga.jupyterclient.path

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Path finder for linux
 */
class LinuxJupyterDirFinder(val sysPrefix: String) {
    companion object {
        val JUPYTER_CONFIG_DIR = JpEnvVars.JUPYTER_CONFIG_DIR
        val JUPYTER_CONFIG_PATH = JpEnvVars.JUPYTER_CONFIG_PATH
        val JUPYTER_DATA_DIR = JpEnvVars.JUPYTER_DATA_DIR
        val JUPYTER_PATH = JpEnvVars.JUPYTER_PATH
        val JUPYTER_RUNTIME_DIR = JpEnvVars.JUPYTER_RUNTIME_DIR
        val SYSTEM_JUPYTER_PATH: List<Path> = listOf(
            "/usr/local/share/jupyter",
            "/usr/share/jupyter"
        ).map { Paths.get(it) }

        val SYSTEM_CONFIG_PATH = listOf(
            "/usr/local/etc/jupyter",
            "/etc/jupyter",
        ).map{Paths.get(it)}
    }

    private val sysPrefixPath: Path = Paths.get(this.sysPrefix).toAbsolutePath()
    private val ENV_JUPYTER_PATH:List<Path> = listOf(sysPrefixPath.resolve("share").resolve("jupyter").toAbsolutePath())
    private val ENV_CONFIG_PATH = listOf(this.sysPrefixPath.resolve("etc").resolve("jupyter").toAbsolutePath())

    /**
     */
    fun findConfigDir(): Path {
        val isNoConfig: Boolean = System.getenv(JpEnvVars.JUPYTER_NO_CONFIG) != null
        if(isNoConfig){
            try {
             return   Files.createTempDirectory("jupyter-clean-cfg").toAbsolutePath()
            } catch (e: IOException) {
                throw RuntimeException("Can't create tmp config directory", e)
            }
        }
        val cp = System.getenv(JUPYTER_CONFIG_DIR)?.let { Paths.get(it).toAbsolutePath() }?: CommonPath.userHomePath.resolve(".jupyter")
        return cp
    }


    fun findConfigPath(): List<Path> {

        if(System.getenv(JpEnvVars.JUPYTER_NO_CONFIG)!=null){
            return listOf(this.findConfigDir())
        }

        val paths:MutableList<Path> = mutableListOf<Path>()
        val configPath = System.getenv(JUPYTER_CONFIG_PATH)?.trim()?.split(File.pathSeparatorChar)?.map { Paths.get(it) } ?: emptyList()
        paths.addAll(configPath)

        val userPath = this.findConfigDir()
        val envPath = ENV_CONFIG_PATH.filter{ !SYSTEM_CONFIG_PATH.contains(it) }
        if(System.getenv(JpEnvVars.JUPYTER_PREFER_ENV_PATH)!=null){
            paths.addAll(envPath)
            paths.add(userPath)
        }else{
            paths.add(userPath)
            paths.addAll(envPath)
        }
        paths.addAll(SYSTEM_CONFIG_PATH)
        return paths
    }

    /**
     * see for detail: https://jupyter.readthedocs.io/en/latest/use/jupyter-directories.html#runtime-files
     * order of preference
     * $XDG_RUNTIME_DIR/jupyter
     * JUPYTER_RUNTIME_DIR
     */
    fun findRuntimeDir(): Path {
        val rt= System.getenv(JUPYTER_RUNTIME_DIR)?.let {
            Paths.get(it).toAbsolutePath()
        } ?: this.findDataDir().resolve("runtime").toAbsolutePath()
        return rt
    }

    fun findDataPath(subdirs:List<Path> = emptyList()): List<Path> {
        val paths:MutableList<Path> = mutableListOf<Path>()
        // JUPYTER_PATH
        val jpPaths: List<Path> = System.getenv(JUPYTER_PATH)
            ?.trim()
            ?.split(File.pathSeparatorChar)
            ?.map { Paths.get(it) } ?: emptyList()
        paths.addAll(jpPaths)

        val userDataDir = this.findDataDir()
        val envDataDir:List<Path> = ENV_JUPYTER_PATH.filter { SYSTEM_JUPYTER_PATH.contains(it).not() }
        if(System.getenv(JpEnvVars.JUPYTER_PREFER_ENV_PATH)!=null){
            paths.addAll(envDataDir)
            paths.add(userDataDir)
        }else{
            paths.add(userDataDir)
            paths.addAll(envDataDir)
        }

        paths.addAll(SYSTEM_JUPYTER_PATH)
        paths.addAll(subdirs)
        return paths
    }

    fun findDataDir(): Path {
        val p1 = (System.getenv(JUPYTER_DATA_DIR))?.let { Paths.get(it).toAbsolutePath() }
        if (p1 != null) {
            return p1
        }

        val p2 = CommonPath.xdgRuntimePath?.resolve("jupyter")?.toAbsolutePath()
        if (p2 != null) {
            return p2
        }

        val p3 = CommonPath.userHomePath.resolve(".local/share/jupyter/").toAbsolutePath()
        return p3
    }
}
