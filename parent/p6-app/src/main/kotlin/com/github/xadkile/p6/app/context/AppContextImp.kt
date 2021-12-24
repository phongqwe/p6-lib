package com.github.xadkile.p6.app.context

import com.github.xadkile.p6.message.api.connection.kernel_context.KernelContext
import com.github.xadkile.p6.message.api.connection.kernel_context.KernelContextReadOnlyConv

class AppContextImp(private val username:String, private val kernelContext:KernelContext) :AppContext{
    override fun getUsername(): String {
        return this.username
    }

    override fun getKernelContextReadOnly(): KernelContextReadOnlyConv {
        return this.kernelContext.conv()
    }
}
