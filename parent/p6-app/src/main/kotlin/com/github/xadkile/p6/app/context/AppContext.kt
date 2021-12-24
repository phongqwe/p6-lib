package com.github.xadkile.p6.app.context

import com.github.xadkile.p6.message.api.connection.kernel_context.KernelContextReadOnlyConv

interface AppContext {
    fun getUsername():String
    fun getKernelContextReadOnly(): KernelContextReadOnlyConv
}
