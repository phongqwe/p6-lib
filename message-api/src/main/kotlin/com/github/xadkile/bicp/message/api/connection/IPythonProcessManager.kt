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
     * Return true if successfully launch IPython kerne, false otherwise.
     */
    fun startIPython(): Result<Unit, Exception>

    /**
     * Kill the current IPython process and delete the current connection file
     */
    fun stopIPython():Result<Unit, Exception>

    fun getIPythonProcess():Process?

    /**
     * Terminate the current process and launch a new IPython Process
     *
     * Connection file content is also updated
     */
    fun restartIPython():Result<Unit, Exception>

    /**
     * Return connection info file content.
     *
     * Connection file info is available for use only when IPython process is launch successfully
     */
    fun getConnectionFileContent(): KernelConnectionFileContent?
}

