package com.github.xadkile.bicp.message.api.connection

import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.protocol.KernelConnectionFileContent
import com.github.xadkile.bicp.message.api.protocol.other.MsgIdGenerator
import java.io.InputStream
import java.io.OutputStream

/**
 * manage IPython process, also provide connection info
 */
interface IPythonContext {
    /**
     * Start IPython process and read connection file.
     *
     * It is guarantee that once IPython start, components objects are available for use. They include: IPython Process, connection file object, session object, channel provider, sender factory.
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
    fun stopIPython(): Result<Unit, Exception>


    /**
     * Terminate the current process and launch a new IPython Process.
     *
     * Connection file content is also updated.
     *
     * This function can only be used on already running manager. Attempt to call it on stopped manager must be prohibited.
     */
    fun restartIPython(): Result<Unit, Exception>


    fun getIPythonProcess(): Result<Process, Exception>

    fun getIPythonInputStream():Result<InputStream,Exception>
    fun getIPythonOutputStream():Result<OutputStream,Exception>

    /**
     * Return connection info file content.
     *
     * Connection file info is available for use only when IPython process is launch successfully
     */
    fun getConnectionFileContent(): Result<KernelConnectionFileContent, Exception>

    fun getSession(): Result<Session, Exception>

    fun getChannelProvider(): Result<ChannelProvider, Exception>

    fun getSenderProvider():Result<SenderProvider,Exception>

    fun getMsgEncoder():Result<MsgEncoder,Exception>

    fun getMsgIdGenerator():Result<MsgIdGenerator,Exception>

    /**
     * add a listener that is invoked before a legal/normal stopping of a process
     */
    fun addOnBeforeProcessStopListener(listener:OnIPythonProcessStoppedListener)

    /**
     * remove the legal/normal on-before-process-stop listener
     */
    fun removeBeforeOnProcessStopListener()

    /**
     * add a listener that is invoked after a legal/normal stopping of a process
     */
    fun addOnAfterProcessStopListener(listener:OnIPythonProcessStoppedListener)

    /**
     * remove the legal/normal on-after-process-stop listener
     */
    fun removeAfterOnProcessStopListener()

    /**
     * Add a listener that is invoked when the ipython process is killed NOT by this app. For example: killed by the OS itself, killed directly by the user using OS tools (system monitor, task manager,etc)
     */
    fun addOnUnexpectedProcessStopListener(listener:OnIPythonProcessStoppedListener)

    /**
     * remove the unexpected-process-stop listener
     */
    fun removeOnProcessUnexpectedStopListener()

}

