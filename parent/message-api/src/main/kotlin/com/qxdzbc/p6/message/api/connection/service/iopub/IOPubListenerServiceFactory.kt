package com.qxdzbc.p6.message.api.connection.service.iopub

import com.qxdzbc.p6.common.exception.error.CommonErrors
import com.qxdzbc.p6.common.exception.error.ErrorReport
import com.qxdzbc.p6.message.api.connection.kernel_context.KernelContextReadOnly
import com.qxdzbc.p6.message.api.connection.kernel_context.KernelTimeOut
import com.qxdzbc.p6.message.api.message.protocol.JPRawMessage
import dagger.assisted.AssistedFactory

@AssistedFactory
interface IOPubListenerServiceFactory{
    fun create(
        kernelContext: KernelContextReadOnly,
        defaultHandler: ((msg: JPRawMessage) -> Unit)? = {},
        parseExceptionHandler: (exception: ErrorReport) -> Unit = {},
        startTimeOut: Long = KernelTimeOut.defaultTimeOut,
    ): IOPubListenerServiceImp
}
