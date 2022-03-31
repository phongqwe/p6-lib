package com.emeraldblast.p6.message.api.message.sender.shell

import com.emeraldblast.p6.common.exception.error.ErrorReport
import com.emeraldblast.p6.message.api.connection.kernel_context.KernelContextReadOnly
import com.emeraldblast.p6.message.api.connection.kernel_context.errors.KernelErrors
import com.emeraldblast.p6.message.api.message.protocol.JPMessage
import com.emeraldblast.p6.message.api.message.protocol.data_interface_definition.Shell
import com.emeraldblast.p6.message.api.message.sender.MsgSender
import com.emeraldblast.p6.message.api.message.sender.ZMQMsgSender
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.unwrap

typealias CommOpenRequest = JPMessage<Shell.Comm.Open.MetaData, Shell.Comm.Open.Content>

class CommOpenSender(
    private val kernelContext: KernelContextReadOnly,
) : MsgSender<CommOpenRequest, Result<Unit, ErrorReport>> {
    override suspend fun send(message: CommOpenRequest): Result<Unit, ErrorReport> {
        if (this.kernelContext.isKernelNotRunning()) {
            return Err(ErrorReport(header = KernelErrors.KernelDown,
                data = KernelErrors.KernelDown.Data(""),
                loc = "${this.javaClass.canonicalName}.send"))
        }

        val out: Result<Unit, ErrorReport> = ZMQMsgSender.sendJPMsgNoReply(
            message,
            kernelContext.getSocketProvider().unwrap().shellSocket(),
            kernelContext.getMsgEncoder().unwrap(),
            kernelContext.getHeartBeatService().unwrap()
        )
        return out
    }
}

