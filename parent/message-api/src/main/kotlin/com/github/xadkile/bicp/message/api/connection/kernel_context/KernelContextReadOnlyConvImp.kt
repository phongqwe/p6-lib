package com.github.xadkile.bicp.message.api.connection.kernel_context

class KernelContextReadOnlyConvImp(private val context: KernelContextReadOnly) : KernelContextReadOnlyConv {
    override fun original(): KernelContextReadOnly {
        return this.context
    }

    override fun conv(): KernelContextReadOnlyConv {
        return this
    }

}
