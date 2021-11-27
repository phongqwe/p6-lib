package com.github.xadkile.bicp.message.api.msg.sender.shell

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.unwrap
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatServiceConv
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelContextReadOnlyConv
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelIsDownException
import com.github.xadkile.bicp.message.api.connection.kernel_context.MsgEncoder
import com.github.xadkile.bicp.message.api.msg.protocol.message.JPMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition.Shell
import com.github.xadkile.bicp.message.api.msg.sender.MsgSender
import com.github.xadkile.bicp.message.api.msg.sender.ZSender
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.zeromq.ZContext
import org.zeromq.ZMQ


typealias KernelInfoInput = JPMessage<Shell.KernelInfo.Request.MetaData, Shell.KernelInfo.Request.Content>
typealias KernelInfoOutput = JPMessage<Shell.KernelInfo.Reply.MetaData, Shell.KernelInfo.Reply.Content>

class KernelInfoSender internal constructor(
    val context:KernelContextReadOnlyConv
) : MsgSender<KernelInfoInput,Result<KernelInfoOutput,Exception>>{

    override suspend fun send(message: KernelInfoInput,dispatcher: CoroutineDispatcher): Result<KernelInfoOutput,Exception> {

        return coroutineScope {
            withContext(dispatcher) {
                if(context.isRunning()){
                    val socket: ZMQ.Socket = context.getSocketProvider().unwrap().shellSocket()
                    val msgEncoder: MsgEncoder = context.getMsgEncoder().unwrap()
                    val hbService: HeartBeatServiceConv = context.getConvHeartBeatService().unwrap()
                    val zContext: ZContext = context.zContext()

                    val zSender = ZSender<KernelInfoInput,KernelInfoOutput>(socket,msgEncoder, hbService, zContext)

                    val rt = zSender.send<Shell.KernelInfo.Request.MetaData, Shell.KernelInfo.Request.Content>(message)
                    rt
                }else{
                    Err(KernelIsDownException("at ${this.javaClass.simpleName}"))
                }
            }
        }
    }
}
