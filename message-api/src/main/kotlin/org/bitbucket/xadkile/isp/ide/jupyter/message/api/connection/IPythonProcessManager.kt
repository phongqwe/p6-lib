package org.bitbucket.xadkile.isp.ide.jupyter.message.api.connection

import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.KernelConnectionFileContent

/**
 * manage IPython process, also provide connection info
 */
interface IPythonProcessManager {
    /**
     * Once IPython process is launch successfully, connection file info must be available for use
     */
    fun launchIPythonProcess(): Boolean

    fun getIPythonProcess():Process

    /**
     * Terminate the current process and launch a new IPython Process
     */
    fun restartIPython():Boolean

    /**
     * get connection info file content
     */
    fun getConnectionInfo(): KernelConnectionFileContent

}
