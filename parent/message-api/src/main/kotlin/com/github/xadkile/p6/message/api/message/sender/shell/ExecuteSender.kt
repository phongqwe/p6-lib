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
import com.github.xadkile.p6.message.api.message.sender.PCSender
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

typealias ExecuteReply = JPMessage<Shell.Execute.Reply.MetaData, Shell.Execute.Reply.Content>

typealias ExecuteRequest = JPMessage<Shell.Execute.Request.MetaData, Shell.Execute.Request.Content>

class ExecuteSender internal constructor(
    private val kernelContext: KernelContextReadOnly,
) : MsgSender<ExecuteRequest,
        Result<ExecuteReply, ErrorReport>> {

    override suspend fun send(
        message: ExecuteRequest,
        dispatcher: CoroutineDispatcher,
    ): Result<ExecuteReply, ErrorReport> {
        if(this.kernelContext.isKernelNotRunning()){
            return Err(
                ErrorReport(
                    type=KernelErrors.KernelDown,
                    data = KernelErrors.KernelDown.Data(""),
                    loc = "${this.javaClass.canonicalName}.send"
                )
            )
        }
        return withContext(dispatcher) {
            val pcSender = PCSender<ExecuteRequest, ExecuteReply>(
                kernelContext.getSocketProvider().unwrap().shellSocket(),
                kernelContext.getMsgEncoder().unwrap(),
                kernelContext.getHeartBeatService().unwrap(),
                kernelContext.zContext(),
                kernelContext.getKernelConfig().timeOut.messageTimeOut
            )
            val rt: Result<ExecuteReply, ErrorReport> =
                pcSender.send2<Shell.Execute.Reply.MetaData, Shell.Execute.Reply.Content>(message)
            rt
        }
    }
}
