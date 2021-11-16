package com.github.xadkile.bicp.message.api.connection.process_watcher

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.unwrapError
import com.github.xadkile.bicp.message.api.exception.CompositeException

/**
 * TODO don't know if this has any use
 */
class MultiThreadProcessWatcher(
    // on stop listener can cache system exit code such as SIGTERM, SIGKILL
    private val onStdinWatcher: OnStdinWatcher,
    private val onStderrWatcher: OnStderrWatcher,
    private val onStopWatcher: OnStopWatcher,
    // all some of the watchers to fail
    private var allowPartialWatching: Boolean = false,
) : ProcessWatcher {

    private var isWatching: Boolean = false
    // this watcher handle three threads, is it even safe to start three thread at the sametime, what if one of them fail?
    // should I allow partial watcher?
    private val watchers = listOf(this.onStderrWatcher,this.onStdinWatcher, this.onStopWatcher)
    override fun startWatching(process: Process): Result<Unit,Exception> {
        val rStderr = this.onStderrWatcher.startWatching(process)
        val rStdout = this.onStdinWatcher.startWatching(process)
        val rStop = this.onStopWatcher.startWatching(process)
        val results = listOf(rStderr, rStdout, rStop)
        if(results.all{it is Err}){
            return Err(CompositeException(listOf(rStderr.unwrapError(),rStdout.unwrapError(),rStop.unwrapError())))
        }else{
            if(allowPartialWatching){
                return Ok(Unit)
            }else{
                val allIsOk = results.all { it is Ok }
                if(allIsOk){
                    return Ok(Unit)
                }else{
                    this.watchers.forEach { it.stopWatching() }
                    return Err(CompositeException(results.filter{it is Err}.map{it.unwrapError()}))
                }
            }
        }
    }

    override fun isWatching(): Boolean {
        if(allowPartialWatching){
            return watchers.any { it.isWatching() }
        }else{
            return watchers.all { it.isWatching() }
        }
    }

    override fun stopWatching(): Result<Unit, Exception> {
        val errs = this.watchers.map { it.stopWatching() }.filter { it is Err }
        if(errs.isEmpty()){
            return Ok(Unit)
        }else{
            return Err(CompositeException(errs.map{it.unwrapError()}))
        }
    }
}
