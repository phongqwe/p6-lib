package com.emeraldblast.p6.message.api.connection.service.zmq_services.imp

import com.emeraldblast.p6.message.api.connection.kernel_context.KernelContextReadOnly
import dagger.assisted.AssistedFactory

@AssistedFactory
interface SyncREPServiceFactory{
    fun create(
        kernelContext: KernelContextReadOnly
    ): SyncREPService
}
