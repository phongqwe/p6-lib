package org.bitbucket.xadkile.taiga.jupyterclient.path

import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Path finder for linux
 * [sysPrefix]  = should be injected from the one that manage the conda env
 */
class LinuxJupyterDirFinder(val sysPrefix: String) : JupyterDirFinder {
    companion object {
        val JUPYTER_CONFIG_DIR = JpEnv.JUPYTER_CONFIG_DIR
        val JUPYTER_CONFIG_PATH = JpEnv.JUPYTER_CONFIG_PATH
        val JUPYTER_DATA_DIR = JpEnv.JUPYTER_DATA_DIR
        val JUPYTER_PATH = JpEnv.JUPYTER_PATH
        val JUPYTER_RUNTIME_DIR = JpEnv.JUPYTER_RUNTIME_DIR
        val SYSTEM_JUPYTER_PATH: List<Path> = listOf(
            "/usr/local/share/jupyter",
            "/usr/share/jupyter"
        ).map { Paths.get(it) }

        val SYSTEM_CONFIG_PATH = listOf(
            "/usr/local/etc/jupyter",
            "/etc/jupyter",
        ).map { Paths.get(it) }

        private fun getSysPrefix(pythonExecutablePath:String):Path{
            // write sys.prefix to stdout
            val getSysPrefixCode="import sys;sys.stdout.write(sys.prefix)"
            val process:Process = ProcessBuilder(pythonExecutablePath,"-c",getSysPrefixCode).start()
            val rt:Path = BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                // read sys.prefix from stdin
                val sysPrefix = reader.readLine()
                Paths.get(sysPrefix)
            }
            return rt
        }

        fun fromPythonExecutable(pythonExecutablePath:String):LinuxJupyterDirFinder{
            return LinuxJupyterDirFinder(getSysPrefix(pythonExecutablePath).toAbsolutePath().toString())
        }
    }

    private val sysPrefixPath: Path = Paths.get(this.sysPrefix).toAbsolutePath()
    private val ENV_JUPYTER_PATH: List<Path> = listOf(
        sysPrefixPath.resolve("share").resolve("jupyter").toAbsolutePath()
    )
    private val ENV_CONFIG_PATH = listOf(this.sysPrefixPath.resolve("etc").resolve("jupyter").toAbsolutePath())

    override fun findConfigDir(): Path {
        val isNoConfig: Boolean = System.getenv(JpEnv.JUPYTER_NO_CONFIG) != null
        if (isNoConfig) {
            try {
                return Files.createTempDirectory("jupyter-clean-cfg").toAbsolutePath()
            } catch (e: IOException) {
                throw RuntimeException("Can't create tmp config directory", e)
            }
        }
        val cp = System.getenv(JUPYTER_CONFIG_DIR)?.let { Paths.get(it).toAbsolutePath() }
            ?: CommonPath.userHomePath.resolve(".jupyter")
        return cp
    }

    override fun findConfigPath(): List<Path> {

        if (System.getenv(JpEnv.JUPYTER_NO_CONFIG) != null) {
            return listOf(this.findConfigDir())
        }

        val paths: MutableList<Path> = mutableListOf<Path>()
        val configPath =
            System.getenv(JUPYTER_CONFIG_PATH)?.trim()?.split(File.pathSeparatorChar)?.map { Paths.get(it) }
                ?: emptyList()
        paths.addAll(configPath)

        val userPath = this.findConfigDir()
        val envPath = ENV_CONFIG_PATH.filter { !SYSTEM_CONFIG_PATH.contains(it) }
        if (System.getenv(JpEnv.JUPYTER_PREFER_ENV_PATH) != null) {
            paths.addAll(envPath)
            paths.add(userPath)
        } else {
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
    override fun findRuntimeDir(): Path {
        val rt = System.getenv(JUPYTER_RUNTIME_DIR)?.let {
            Paths.get(it).toAbsolutePath()
        } ?: this.findDataDir().resolve("runtime").toAbsolutePath()
        return rt
    }

    override fun findRuntimePath(): List<Path> {
        return listOf(this.findRuntimeDir())
    }

    override fun findDataDir(): Path {
        val p1 = (System.getenv(JUPYTER_DATA_DIR))?.let { Paths.get(it).toAbsolutePath() }
        if (p1 != null) {
            return p1
        }

        val p2 = CommonPath.xdgDataHome?.resolve("jupyter")?.toAbsolutePath()
        if (p2 != null) {
            return p2
        }

        val p3 = CommonPath.userHomePath.resolve(".local/share/jupyter/").toAbsolutePath()
        return p3
    }

    override fun findDataPath(): List<Path> {
        val paths: MutableList<Path> = mutableListOf<Path>()
        // JUPYTER_PATH
        val jpPaths: List<Path> = System.getenv(JUPYTER_PATH)
            ?.trim()
            ?.split(File.pathSeparatorChar)
            ?.map { Paths.get(it) } ?: emptyList()
        paths.addAll(jpPaths)

        val userDataDir = this.findDataDir()
        val envDataDir: List<Path> = ENV_JUPYTER_PATH.filter { SYSTEM_JUPYTER_PATH.contains(it).not() }
        if (System.getenv(JpEnv.JUPYTER_PREFER_ENV_PATH) != null) {
            paths.addAll(envDataDir)
            paths.add(userDataDir)
        } else {
            paths.add(userDataDir)
            paths.addAll(envDataDir)
        }

        paths.addAll(SYSTEM_JUPYTER_PATH)
        return paths
    }

//    override fun findSubDataPath(subdirs: List<Path>): List<Path> {
//        val dataPath = this.findDataPath()
//        val subPathList = dataPath.flatMap { path->
//            subdirs.map{subdir->
//                path.resolve(subdir).toAbsolutePath()
//            }
//        }
//        return subPathList
//    }
//
//    override fun findSubDataPathInclude(subdirs:List<Path>):List<Path>{
//        return this.findDataPath() + this.findSubDataPath(subdirs)
//    }

    override fun findIPythonDir(): Path {
        val ipythonDirName = ".ipython"
        val userHome:Path = CommonPath.userHomePath
//        val xdgConfig:Path? = CommonPath.xdgConfigPath
        val explicitIpPath:Path? = (System.getenv(JpEnv.IPYTHONDIR) ?: System.getenv(JpEnv.IPYTHON_DIR_DEPRECATED))?.let{
            Paths.get(it).toAbsolutePath()
        }
        val ipPath = if(explicitIpPath==null){
            val ipPathOnHome = userHome.resolve(ipythonDirName)
            ipPathOnHome
        }else{
            return explicitIpPath
        }
        val rt:Path = if(Files.exists(ipPath)){
            if(false == Files.isWritable(ipPath)){
                val tempDir = Files.createTempDirectory("/tmp")
                println("IPython directory: $ipPath is not writable, use this temp directory instead: $tempDir")
                tempDir
            }else{
                ipPath
            }
        }else if(false == Files.exists(ipPath)){
            if(false == Files.exists(ipPath.parent)){
                val tempDir = Files.createTempDirectory("/tmp")
                println("IPython parent directory: ${ipPath.parent} is not writable, use this temp directory instead: $tempDir")
                tempDir
            }else{
                ipPath
            }
        }else{
            ipPath
        }
        return rt
    }
}
