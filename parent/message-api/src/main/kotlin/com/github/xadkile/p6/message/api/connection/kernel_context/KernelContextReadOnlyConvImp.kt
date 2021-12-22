package com.github.xadkile.p6.message.api.connection.kernel_context

import com.github.michaelbull.result.Result
import com.github.xadkile.p6.message.api.connection.service.iopub.IOPubListenerService

class KernelContextReadOnlyConvImp(private val context: KernelContextReadOnly) : KernelContextReadOnlyConv {
    override fun original(): KernelContextReadOnly {
        return this.context
    }

    override fun conv(): KernelContextReadOnlyConv {
        return this
    }

}
