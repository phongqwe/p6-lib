package com.github.xadkile.p6.message.api.connection.service.zmq_services

import com.github.xadkile.p6.message.api.connection.kernel_context.KernelContextReadOnly
import com.github.xadkile.p6.message.api.connection.service.zmq_services.imp.REPService
import com.github.xadkile.p6.message.api.connection.service.zmq_services.imp.SUBService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

class ZMQSocketListenerServices(
    private val kernelContext: KernelContextReadOnly,
    private val defaultCoroutineScope: CoroutineScope,
    private val defaultCoroutineDispatcher: CoroutineDispatcher,
) {

    fun newSub(
        coroutineScope: CoroutineScope = defaultCoroutineScope,
        coroutineDispatcher: CoroutineDispatcher = defaultCoroutineDispatcher,
    ): ZMQSocketListenerService {
        return SUBService(kernelContext, coroutineScope, coroutineDispatcher)
    }

    fun newRep(coroutineScope: CoroutineScope = defaultCoroutineScope,
               coroutineDispatcher: CoroutineDispatcher = defaultCoroutineDispatcher,):ZMQSocketListenerService{
        return REPService(kernelContext, coroutineScope, coroutineDispatcher)
    }

}
