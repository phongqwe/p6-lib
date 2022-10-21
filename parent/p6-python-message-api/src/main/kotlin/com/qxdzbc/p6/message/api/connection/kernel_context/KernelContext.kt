package com.qxdzbc.p6.message.api.connection.kernel_context

import com.github.michaelbull.result.Result
import com.qxdzbc.common.error.ErrorReport
import com.qxdzbc.p6.message.api.message.protocol.KernelConnectionFileContent
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path

/**
 * TODO need to add something to watch for unexpected kill of IPython
 * Interface for managing kernel process, provide information to work with the kernel.
 * There is a risk of memory leak here. It is crucial that consumers of instances of this interface must not cache any derivative objects
 */
interface KernelContext : KernelContextReadOnly {

    val kernelStatus:KernelStatus
    val isLoggerEnabled:Boolean
    fun enableLogger():KernelContext
    fun disableLogger():KernelContext

    fun setKernelConfig(kernelConfig: KernelConfig):KernelContext
    fun setConnectionFileContent(connectionFileContent: KernelConnectionFileContent):KernelContext
    fun setConnectionFilePath(connectionFilePath: Path):KernelContext
    /**
     * startCore() + startServices
     */
    fun startAll(): Result<Unit, ErrorReport>

    /**
     * Start IPython process and read connection file produced by Ipython.
     *
     * It is guarantee that once IPython start, components objects are available for use. They include: IPython Process, connection file object, session object, channel provider, sender factory.
     *
     * Call [startKernel] on an already running context does not change the state of this manager, return Ok result.
     *
     * It must be guaranteed that connection file is created and read, and process (jpython + zmq) is on and ready to accept command.
     */
    fun startKernel():Result<Unit, ErrorReport>
    fun startKernel(kernelConfig: KernelConfig): Result<Unit, ErrorReport>
    fun startKernel(connectionFilePath: Path): Result<Unit, ErrorReport>
    fun startKernel(connectionFileContent: KernelConnectionFileContent): Result<Unit, ErrorReport>


    /**
     * start services
     */

    /**
     * Kill the current kernel process and delete the current connection file.
     * Calling this on an already stopped kernel context does nothing.
     * It guarantees that connection file is deleted, kernel process is completely killed after calling stop.
     */
    fun stopAll(): Result<Unit, ErrorReport>

    /**
     * stop the kernel thread
     */
     fun stopKernel():Result<Unit, ErrorReport>

    /**
     * Terminate the current process and launch a new IPython Process.
     * Connection file content is also updated.
     * This function can only be used on already running manager. Attempt to call it on stopped manager must be prohibited.
     */
    fun restartKernel(): Result<Unit, ErrorReport>
    fun restartKernel(kernelConfig: KernelConfig): Result<Unit, ErrorReport>
    fun restartKernel(connectionFilePath: Path): Result<Unit, ErrorReport>
    fun restartKernel(connectionFileContent: KernelConnectionFileContent): Result<Unit, ErrorReport>

    fun getKernelProcess(): Result<Process, ErrorReport>

    /**
     * Return input stream of the current IPython process
     */
    fun getKernelInputStream():Result<InputStream, ErrorReport>

    /**
     * Return output stream of the current IPython process
     */
    fun getKernelOutputStream():Result<OutputStream, ErrorReport>

    /**
     * add a listener that is invoked before a legal/normal stopping of a process
     */
    fun setOnBeforeStopListener(listener: OnKernelContextEvent)

    /**
     * remove the legal/normal on-before-process-stop listener
     */
    fun removeBeforeStopListener()

    /**
     * add a listener that is invoked after a legal/normal stopping of a process
     */
    fun setOnAfterStopListener(listener: OnKernelContextEvent)

    /**
     * remove the legal/normal on-after-process-stop listener
     */
    fun removeAfterStopListener()

    fun setKernelStartedListener(listener: OnKernelContextEvent)

    fun removeOnProcessStartListener()
    fun getConnectionFilePath(): Path?
}

