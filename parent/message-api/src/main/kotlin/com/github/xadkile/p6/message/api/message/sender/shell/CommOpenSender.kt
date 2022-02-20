package com.github.xadkile.p6.message.api.message.sender.shell

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.unwrap
import com.github.xadkile.p6.common.exception.lib.error.ErrorReport
import com.github.xadkile.p6.message.api.connection.kernel_context.KernelContextReadOnly
import com.github.xadkile.p6.message.api.connection.kernel_context.errors.KernelErrors
import com.github.xadkile.p6.message.api.message.protocol.JPMessage
import com.github.xadkile.p6.message.api.message.protocol.data_interface_definition.Shell
import com.github.xadkile.p6.message.api.message.sender.MsgSender
import com.github.xadkile.p6.message.api.message.sender.ZMQMsgSender
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

typealias CommOpenRequest = JPMessage<Shell.Comm.Open.MetaData, Shell.Comm.Open.Content>

class CommOpenSender(
    private val kernelContext: KernelContextReadOnly,
) : MsgSender<CommOpenRequest, Result<Unit, ErrorReport>> {
    override suspend fun send(message: CommOpenRequest, dispatcher: CoroutineDispatcher): Result<Unit, ErrorReport> {
        if (this.kernelContext.isKernelNotRunning()) {
            return Err(ErrorReport(type = KernelErrors.KernelDown,
                data = KernelErrors.KernelDown.Data(""),
                loc = "${this.javaClass.canonicalName}.send"))
        }
        val rt = withContext(dispatcher) {
            val out: Result<Unit, ErrorReport> = ZMQMsgSender.sendJPMsgNoReply(
                message,
                kernelContext.getSocketProvider().unwrap().shellSocket(),
                kernelContext.getMsgEncoder().unwrap(),
                kernelContext.getHeartBeatService().unwrap()
            )
            out
        }
        return rt
    }
}

