package com.github.xadkile.bicp.message.api.connection

import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.protocol.KernelConnectionFileContent

/**
 * manage IPython process, also provide connection info
 */
interface IPythonProcessManager {
    /**
     * Start IPython process and read connection file.
     *
     * It is guarantee that once IPython start, a connection file and a Process object are available for use
     *
     * run [startIPython] on a already running manager does not change the state of this manager, return Ok result
     *
     * Return true if successfully launch IPython kerne, false otherwise.
     *
     * It must be guaranteed that connection file is created and read, and process (jpython + zmq) is on and ready to accept command.
     */
    fun startIPython(): Result<Unit, Exception>

    /**
     * Kill the current IPython process and delete the current connection file
     *
     * Stop an already stopped manager does nothing, return Ok result
     *
     * It must be guaranteed that connection file is deleted, process is completely killed after calling stop.
     */
    fun stopIPython():Result<Unit, Exception>

    fun getIPythonProcess():Process?

    /**
     * Terminate the current process and launch a new IPython Process.
     *
     * Connection file content is also updated.
     *
     * This function can only be used on already running manager. Attempt to call it on stopped manager must be prohibited.
     */
    fun restartIPython():Result<Unit, Exception>

    /**
     * Return connection info file content.
     *
     * Connection file info is available for use only when IPython process is launch successfully
     */
    fun getConnectionFileContent(): KernelConnectionFileContent?

    fun getSession():Session?

    fun getChannelProvider():ChannelProvider?
}

