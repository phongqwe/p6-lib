package com.emeraldblast.p6.message.api.message.sender.shell

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.unwrap
import com.emeraldblast.p6.common.exception.error.ErrorReport
import com.emeraldblast.p6.message.api.connection.kernel_context.KernelContextReadOnly
import com.emeraldblast.p6.message.api.connection.kernel_context.errors.KernelErrors
import com.emeraldblast.p6.message.api.message.protocol.JPMessage
import com.emeraldblast.p6.message.api.message.protocol.data_interface_definition.Shell
import com.emeraldblast.p6.message.api.message.sender.MsgSender
import com.emeraldblast.p6.message.api.message.sender.PCSender

typealias ExecuteReply = JPMessage<Shell.Execute.Reply.MetaData, Shell.Execute.Reply.Content>

typealias ExecuteRequest = JPMessage<Shell.Execute.Request.MetaData, Shell.Execute.Request.Content>

class ExecuteSender internal constructor(
    private val kernelContext: KernelContextReadOnly,
) : MsgSender<ExecuteRequest,
        Result<ExecuteReply, ErrorReport>> {

    override suspend fun send(
        message: ExecuteRequest,
    ): Result<ExecuteReply, ErrorReport> {
        if (this.kernelContext.isKernelNotRunning()) {
            return Err(
                ErrorReport(
                    header = KernelErrors.KernelDown.header,
                    data = KernelErrors.KernelDown.Data(""),
                )
            )
        }
        val pcSender = PCSender<ExecuteRequest, ExecuteReply>(
            kernelContext.getSocketProvider().unwrap().shellSocket(),
            kernelContext.getMsgEncoder().unwrap(),
            kernelContext.getHeartBeatService().unwrap(),
            kernelContext.zContext(),
            kernelContext.getKernelConfig().timeOut.messageTimeOut
        )
        val rt: Result<ExecuteReply, ErrorReport> =
            pcSender.send2<Shell.Execute.Reply.MetaData, Shell.Execute.Reply.Content>(message)
        return rt
    }
}
