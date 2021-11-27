package com.github.xadkile.bicp.message.api.msg.sender.shell

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.unwrap
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatServiceConv
import com.github.xadkile.bicp.message.api.connection.ipython_context.*
import com.github.xadkile.bicp.message.api.msg.protocol.message.JPMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition.Shell
import com.github.xadkile.bicp.message.api.msg.sender.MsgSender
import com.github.xadkile.bicp.message.api.msg.sender.ZSender
import kotlinx.coroutines.*
import org.zeromq.ZContext
import org.zeromq.ZMQ

typealias ExecuteReply = JPMessage<Shell.Execute.Reply.MetaData, Shell.Execute.Reply.Content>

typealias ExecuteRequest = JPMessage<Shell.Execute.Request.MetaData, Shell.Execute.Request.Content>

class ExecuteSender internal constructor(
    val kernelContext: KernelContextReadOnlyConv,
) : MsgSender<ExecuteRequest,
        Result<ExecuteReply, Exception>> {

    override suspend fun send(
        message: ExecuteRequest,
        dispatcher: CoroutineDispatcher,
    ): Result<ExecuteReply, Exception> {
        return coroutineScope {
            withContext(dispatcher) {
                if (kernelContext.isRunning()) {
                    val zsender = ZSender<ExecuteRequest, ExecuteReply>(
                        kernelContext.getSocketProvider().unwrap().shellSocket(),
                        kernelContext.getMsgEncoder().unwrap(),
                        kernelContext.getConvHeartBeatService().unwrap(),
                        kernelContext.zContext())
                    val rt = zsender.send<Shell.Execute.Reply.MetaData, Shell.Execute.Reply.Content>(message)
                    rt
                } else {
                    Err(KernelIsDownException("kernel is down"))
                }
            }
        }
    }
}
