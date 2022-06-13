package com.emeraldblast.p6.message.api.connection.kernel_context.context_object

import com.emeraldblast.p6.message.api.connection.kernel_context.KernelContextReadOnly
import dagger.assisted.AssistedFactory

@AssistedFactory
interface SenderProviderFactory{
    fun create(
//        kernelContext: KernelContextReadOnly
    ): SenderProviderImp
}
