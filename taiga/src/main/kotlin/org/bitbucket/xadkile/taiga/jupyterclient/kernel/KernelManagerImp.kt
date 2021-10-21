package org.bitbucket.xadkile.taiga.jupyterclient.kernel

import java.nio.file.Files
import java.nio.file.Path

class KernelManagerImp : KernelManager {
    // start a kernel return a kernel client


    // TODO done, findout how to create args for this function
    fun makeProcessBuilder(
        connectionFile: KernelConnectionFileContent, //from where: generate it myself
        commandArgs: List<String>, // from kernel spec
        runtimePath: Path?, // from JupyterPathFinder
        currentWorkingDirectory: Path, // ???
        envVars:Map<String,String>
    ):ProcessBuilder {
        //write connection file
        val cfPath = this.writeConnectionFile(connectionFile, runtimePath)
        val newArgs = this.addConnectionFilePathToArgs(commandArgs,cfPath)
        val pb = ProcessBuilder()
        pb.command(newArgs)
        pb.environment().putAll(envVars)
        pb.directory(currentWorkingDirectory.toFile())
        return pb
    }



    private fun writeConnectionFile(connectionFile: KernelConnectionFileContent, runtimePath: Path?): Path {
        val connectionfilePath = if (runtimePath != null) {
            Files.createTempFile(runtimePath, "kernel-connection-file", "json")
        } else {
            Files.createTempFile("kernel-connection-file", "json")
        }.toAbsolutePath()
        Files.write(connectionfilePath, connectionFile.toJson().toByteArray(Charsets.UTF_8))
        return connectionfilePath
    }

    private fun addConnectionFilePathToArgs(commandArgs: List<String>, connectionFilePath:Path):List<String>{
        val argsWithConnectionFile = commandArgs.map{
            if(it=="{connection_file}"){
                connectionFilePath.toString()
            }else{
                it
            }
        }
        return argsWithConnectionFile
    }

    fun startKernel(): KernelClient {
        return KernelClientImp()
    }
}


