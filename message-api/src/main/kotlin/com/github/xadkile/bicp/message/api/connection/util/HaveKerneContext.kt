package com.github.xadkile.bicp.message.api.connection.util

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelContextReadOnly
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelIsDownException

interface HaveKernelContext {
    fun getKernelContext():KernelContextReadOnly

    /**
     * check if the kernel context is running before return the result of [function]
     */
    suspend fun <T> checkContextRunningThen(function:suspend ()->Result<T,Exception>):Result<T,Exception>{
        if(this.getKernelContext().isNotRunning()){
            return Err(KernelIsDownException.occurAt(this))
        }else{
            return function()
        }
    }
}

