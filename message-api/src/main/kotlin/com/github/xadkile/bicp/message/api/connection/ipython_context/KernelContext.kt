package com.github.xadkile.bicp.message.api.connection.ipython_context

import com.github.michaelbull.result.Result
import java.io.InputStream
import java.io.OutputStream

/**
 * manage IPython process, also provide connection info.
 * This class is stateful.
 * TODO long: need to add something to watch for unexpected kill of IPython
 * There is a risk of memory leak here. Objects produced by context can be hold by other objects, therefore if this context die, the legality of such objects become questionable, but their references in external objects are still valid, therefore, they are not cleaned up => leak faulty object, and their usage is dangerous. I must take measure to prevent that from happening.
 *
 * For example:
 * heartbeat service can be used by sender to check zmq liveness.
 * During a long computation session, the hb service is polled to make sure that it is safe to continue waiting for the computation to complete.
 * If IpythonContext stops midway or ipython process is killed, it will stop the hb service + null the reference to the service. The consequence is:
 *  - The hb is stopped => the poll will fail => sender return fail result
 *      + if the sender is reused, it will re-used the discarded hb instances => always return fail result even when the zmq is restored.
 *      + if the sender is kept around, the discarded hb instance is kept around => mem leak
 *  - if IPython context is start again, there will be a new hb service instance that is completely different from the discarded instance
 *
 *  Solution:
 *  1. never cached leakable instances as properties, always use method call from context to retrieve their instances. The only cached object is the context instance
 *  2. create an elaborated structure to host leakable instances, this structure must react on state change event of context to retrieve the correct instances. This is just solution 1 with an abstract wall insert between the context and the users. => no added value, just more complexity.
 */
interface KernelContext : KernelContextReadOnly {
    /**
     * Start IPython process and read connection file.
     *
     * It is guarantee that once IPython start, components objects are available for use. They include: IPython Process, connection file object, session object, channel provider, sender factory.
     *
     * Call [startKernel] on an already running manager does not change the state of this manager, return Ok result.
     *
     * Return true if successfully launch the kernel, false otherwise.
     *
     * It must be guaranteed that connection file is created and read, and process (jpython + zmq) is on and ready to accept command.
     */
    fun startKernel(): Result<Unit, Exception>

    /**
     * Kill the current kernel process and delete the current connection file.
     *
     * Stop an already stopped manager does nothing, return Ok result.
     *
     * It must be guaranteed that connection file is deleted, process is completely killed after calling stop.
     */
    fun stopKernel(): Result<Unit, Exception>

    /**
     * Terminate the current process and launch a new IPython Process.
     *
     * Connection file content is also updated.
     *
     * This function can only be used on already running manager. Attempt to call it on stopped manager must be prohibited.
     */
    fun restartIPython(): Result<Unit, Exception>

    fun getIPythonProcess(): Result<Process, Exception>

    /**
     * Return input stream of the current IPython process
     */
    fun getIPythonInputStream():Result<InputStream,Exception>

    /**
     * Return output stream of the current IPython process
     */
    fun getIPythonOutputStream():Result<OutputStream,Exception>

    /**
     * add a listener that is invoked before a legal/normal stopping of a process
     */
    fun setOnBeforeProcessStopListener(listener: OnIPythonContextEvent)

    /**
     * remove the legal/normal on-before-process-stop listener
     */
    fun removeBeforeOnProcessStopListener()

    /**
     * add a listener that is invoked after a legal/normal stopping of a process
     */
    fun setOnAfterProcessStopListener(listener: OnIPythonContextEvent)

    /**
     * remove the legal/normal on-after-process-stop listener
     */
    fun removeAfterOnProcessStopListener()

    fun setOnStartProcessListener(listener: OnIPythonContextEvent)

    fun removeOnProcessStartListener()
}

