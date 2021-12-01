package com.github.xadkile.bicp.message.api.msg.sender.shell

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.unwrap
import com.github.xadkile.bicp.message.api.connection.kernel_context.*
import com.github.xadkile.bicp.message.api.msg.protocol.message.JPMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition.Shell
import com.github.xadkile.bicp.message.api.msg.sender.MsgSender
import com.github.xadkile.bicp.message.api.msg.sender.PCSender
import kotlinx.coroutines.*

typealias ExecuteReply = JPMessage<Shell.ExecuteReply.MetaData, Shell.ExecuteReply.Content>

typealias ExecuteRequest = JPMessage<Shell.ExecuteRequest.MetaData, Shell.ExecuteRequest.Content>

class ExecuteSender internal constructor(
    private val kernelContext: KernelContextReadOnlyConv,
) : MsgSender<ExecuteRequest,
        Result<ExecuteReply, Exception>> {

    override suspend fun send(
        message: ExecuteRequest,
        dispatcher: CoroutineDispatcher,
    ): Result<ExecuteReply, Exception> {
        if(this.kernelContext.isNotRunning()){
            return Err(KernelIsDownException.occurAt(this))
        }
        return withContext(dispatcher) {
            val pcSender = PCSender<ExecuteRequest, ExecuteReply>(
                kernelContext.getSocketProvider().unwrap().shellSocket(),
                kernelContext.getMsgEncoder().unwrap(),
                kernelContext.getConvHeartBeatService().unwrap(),
                kernelContext.zContext())
            val rt: Result<ExecuteReply, Exception> =
                pcSender.send<Shell.ExecuteReply.MetaData, Shell.ExecuteReply.Content>(message)
            rt
        }

    }
}
