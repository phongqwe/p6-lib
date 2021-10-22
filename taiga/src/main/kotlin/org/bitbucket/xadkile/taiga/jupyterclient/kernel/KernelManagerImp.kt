package org.bitbucket.xadkile.taiga.jupyterclient.kernel

import org.bitbucket.xadkile.taiga.jupyterclient.kernel.spec.KernelSpecManager
import org.bitbucket.xadkile.taiga.jupyterclient.path.LinuxJupyterDirFinder
import java.nio.file.Files
import java.nio.file.Path

class KernelManagerImp : KernelManager {
    // start a kernel return a kernel client


    // TODO done, findout how to create args for this function
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
    ): KernelClient {
        this.makeProcessBuilder(connectionFile, commandArgs, runtimePath, currentWorkingDirectory, envVars).inheritIO()
            .start()
        return KernelClientImp(connectionFile)
    }

    fun startKernel():KernelClient{
        val cf = KernelConnectionFileContent.makeSHA256LocalOne("key-abc")
        val dirFinder = LinuxJupyterDirFinder("/home/abc/Applications/anaconda3/envs/dl_hw_01")
        val specMan = KernelSpecManager.fromDirFinder(dirFinder)
        val spec = specMan.getKernelSpec("python3")
        val commandArgs = spec.argv
        val env = spec.env
        return this.startKernel(cf,commandArgs,dirFinder.findRuntimeDir(),null,env)
    }
}


