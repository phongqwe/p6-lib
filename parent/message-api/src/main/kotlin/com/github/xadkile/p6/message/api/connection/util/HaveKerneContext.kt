package com.github.xadkile.p6.message.api.connection.util

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.xadkile.p6.message.api.connection.kernel_context.KernelContextReadOnly
import com.github.xadkile.p6.message.api.connection.kernel_context.exception.KernelIsDownException

/**
 * a utility interface providing a shortcut function to check for kernel running status
 */
interface HaveKernelContext {
    fun getKernelContext():KernelContextReadOnly

    /**
     * check if the kernel context is running before return the result of [provider]
     */
    suspend fun <T> checkContextRunningThen(provider:suspend ()->Result<T,Exception>):Result<T,Exception>{
        if(this.getKernelContext().isKernelNotRunning()){
            return Err(KernelIsDownException.occurAt(this))
        }else{
            return provider()
        }
    }
}

