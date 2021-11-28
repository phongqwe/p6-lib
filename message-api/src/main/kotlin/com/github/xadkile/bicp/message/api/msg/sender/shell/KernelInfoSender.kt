package com.github.xadkile.bicp.message.api.msg.sender.shell

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.unwrap
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatServiceConv
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelContextReadOnly
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelContextReadOnlyConv
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelIsDownException
import com.github.xadkile.bicp.message.api.connection.kernel_context.MsgEncoder
import com.github.xadkile.bicp.message.api.msg.protocol.message.JPMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition.Shell
import com.github.xadkile.bicp.message.api.msg.sender.MsgSender
import com.github.xadkile.bicp.message.api.msg.sender.PCSender
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.zeromq.ZContext
import org.zeromq.ZMQ


typealias KernelInfoInput = JPMessage<Shell.KernelInfo.Request.MetaData, Shell.KernelInfo.Request.Content>
typealias KernelInfoOutput = JPMessage<Shell.KernelInfo.Reply.MetaData, Shell.KernelInfo.Reply.Content>

class KernelInfoSender internal constructor(
    val kernelContext: KernelContextReadOnlyConv,
) : MsgSender<KernelInfoInput, Result<KernelInfoOutput, Exception>> {

    override suspend fun send(
        message: KernelInfoInput,
        dispatcher: CoroutineDispatcher,
    ): Result<KernelInfoOutput, Exception> {
        return this.checkContextRunningThen {
            withContext(dispatcher) {
                val socket: ZMQ.Socket = kernelContext.getSocketProvider().unwrap().shellSocket()
                val msgEncoder: MsgEncoder = kernelContext.getMsgEncoder().unwrap()
                val hbService: HeartBeatServiceConv = kernelContext.getConvHeartBeatService().unwrap()
                val zContext: ZContext = kernelContext.zContext()
                val zSender = PCSender<KernelInfoInput, KernelInfoOutput>(socket, msgEncoder, hbService, zContext)
                val rt = zSender.send<Shell.KernelInfo.Request.MetaData, Shell.KernelInfo.Request.Content>(message)
                rt
            }
        }
    }

    override fun getKernelContext(): KernelContextReadOnly {
        return this.kernelContext
    }
}
