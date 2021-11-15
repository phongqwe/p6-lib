package com.github.xadkile.bicp.message.api.connection.process_watcher

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.concurrent.thread

/**
 * Each listener is run a separated thread
 */
class MultiThreadProcessWatcher(
    // on stop listener can cache system exit code such as SIGTERM, SIGKILL
    private var onStopListener: OnStopEventProcessListener = OnStopEventProcessListener.nothing,
    private var onErrListener: OnErrEventProcessListener = OnErrEventProcessListener.nothing,
    private var onStdoutListener: OnStdOutEventProcessListener = OnStdOutEventProcessListener.nothing,
    private var onStdErrListener: OnStdErrEventProcessListener = OnStdErrEventProcessListener.nothing,
) : ProcessWatcher {

    private var processIsStopped = false
    private var process: Process?=null
    private var onStopThread: Thread? = null
    private var onStdOutThread: Thread? = null
    private var onStdErrThread: Thread? = null

    override fun startWatching(process: Process): Result<Unit, Exception> {
        try {
            this.process = process
            this.processIsStopped = process.isAlive.not()
            this.onStdOutThread = thread(true) {
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                while (processIsStopped.not()) {
                    val line = reader.readLine()
                    try {
                        if (line == null) {
                            break
                        } else {
                            this.onStdoutListener.onStdOut(this.process!!, line)
                        }
                    } catch (e: Exception) {
                        this.onErrListener.onError(this.process!!, e)
                        break
                    }
                }
            }
            this.onStdErrThread = thread(true) {
                val reader = BufferedReader(InputStreamReader(process.errorStream))
                try {
                    while (processIsStopped.not()) {
                        try {
                            val line = reader.readLine()
                            if (line == null) {
                                break
                            } else {
                                this.onStdErrListener.onStdErr(this.process!!, line)
                            }
                        } catch (e: Exception) {
                            this.onErrListener.onError(this.process!!, e)
                        }
                    }
                } catch (e: Exception) {
                    this.onErrListener.onError(this.process!!, e)
                }
            }
            this.onStopThread = thread(true) {
                try {
                    this.process!!.waitFor()
                    this.onStopListener.onStop(this.process!!)
                } catch (e: InterruptedException) {
                    this.onErrListener.onError(this.process!!, e)
                } finally {
                    this.processIsStopped = true
                }
            }
            return Ok(Unit)
        } catch (e: Exception) {
            return Err(e)
        }
    }

    override fun stopWatching(): Result<Unit, Exception> {
        try {

            if(this.onStopThread!=null){
                this.onStopThread?.interrupt()
            }
            if(this.onStdOutThread!=null){
                this.onStdOutThread?.interrupt()
            }
            if(this.onStdErrThread!=null){
                this.onStdErrThread?.interrupt()
            }

            return Ok(Unit)
        } catch (e: Exception) {
            return Err(e)
        }
    }

    override fun setStopEventListener(listener: OnStopEventProcessListener) {
        this.onStopListener = listener
    }

    override fun setStdOutEventListener(listener: OnStdOutEventProcessListener) {
        this.onStdoutListener = listener
    }

    override fun setErrEventProcessListener(listener: OnErrEventProcessListener) {
        this.onErrListener = listener
    }

    override fun setOnStdErrEventProcessListener(listener: OnStdErrEventProcessListener) {
        this.onStdErrListener = listener
    }
}
