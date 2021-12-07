package com.github.xadkile.bicp.message.api.connection.kernel_context

fun interface OnKernelContextEvent {
    fun run(context: KernelContext)
    companion object {
        val Nothing = OnKernelContextEvent {
            // do nothing
        }
    }
}
