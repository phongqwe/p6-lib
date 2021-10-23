package org.bitbucket.xadkile.taiga.jupyterclient.kernel

import org.bitbucket.xadkile.taiga.jupyterclient.kernel.spec.KernelSpecManager
import org.bitbucket.xadkile.taiga.jupyterclient.path.LinuxJPDirFinder
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class KernelManagerImp : KernelManager {

    fun makeProcessBuilder(
        connectionFile: KernelConnectionFileContent, //from where: generate it myself
        commandArgs: List<String>, // from kernel spec
        runtimePath: Path?, // from JupyterPathFinder
        currentWorkingDirectory: Path?, // ??? maybe null
        envVars: Map<String, String> // from kernel spec
    ): ProcessBuilder {
        //write connection file
        val cfPath = this.writeConnectionFile(connectionFile, runtimePath)
        val newArgs = this.addConnectionFilePathToArgs(commandArgs, cfPath)
        val pb = ProcessBuilder()
        pb.command(newArgs)
        pb.environment().putAll(envVars)
        currentWorkingDirectory?.also {
            pb.directory(it.toFile())
        }
        return pb
    }

    private fun writeConnectionFile(connectionFile: KernelConnectionFileContent, runtimePath: Path?): Path {
        val kernelConFileName = "kernel-connection-file-"
        val connectionfilePath = if (runtimePath != null) {
            Files.createTempFile(runtimePath, kernelConFileName, ".json")
        } else {
            Files.createTempFile(kernelConFileName, ".json")
        }.toAbsolutePath()
        Files.write(connectionfilePath, connectionFile.toJson().toByteArray(Charsets.UTF_8))
        return connectionfilePath
    }

    private fun addConnectionFilePathToArgs(commandArgs: List<String>, connectionFilePath: Path): List<String> {
        val argsWithConnectionFile = commandArgs.map {
            if (it == "{connection_file}") {
                connectionFilePath.toString()
            } else {
                it
            }
        }
        return argsWithConnectionFile
    }

    fun startKernel(
        connectionFile: KernelConnectionFileContent, //from where: generate it myself
        commandArgs: List<String>, // from kernel spec
        runtimePath: Path?, // from JupyterPathFinder
        currentWorkingDirectory: Path?, // ??? maybe null
        envVars: Map<String, String> // from kernel spec
    ): Kernel {
        val p = this.makeProcessBuilder(connectionFile, commandArgs, runtimePath, currentWorkingDirectory, envVars)
            .inheritIO()
            .start()
        return KernelImp(connectionFile,p)
    }

    fun getSysPrefix(pythonExecutablePath:String):Path{
        val process:Process = ProcessBuilder(pythonExecutablePath,"-c","import sys;sys.stdout.write(sys.prefix)").start()
        val rt:Path = BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            val sysPrefix = reader.readLine()
            Paths.get(sysPrefix)
        }
        return rt
    }
    fun startKernel(pythonExecutablePath: String):Kernel{
        val cf = KernelConnectionFileContent.makeSHA256LocalOne("key-abc")
        val dirFinder = LinuxJPDirFinder.fromPythonExecutable(pythonExecutablePath)
        val specMan = KernelSpecManager.fromDirFinder(dirFinder)
        val spec = specMan.getKernelSpecByName("python3")
        val commandArgs = spec.argv
        val env = spec.env
        return this.startKernel(cf,commandArgs,dirFinder.findRuntimeDir(),null,env)
    }
}


