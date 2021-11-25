package com.github.xadkile.bicp.message.api.msg.sender.shell

import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatServiceConv
import com.github.xadkile.bicp.message.api.connection.ipython_context.MsgEncoder
import com.github.xadkile.bicp.message.api.msg.sender.MsgSender
import com.github.xadkile.bicp.message.api.msg.sender.ZSender
import com.github.xadkile.bicp.message.api.msg.protocol.message.JPMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition.Shell
import org.zeromq.ZContext
import org.zeromq.ZMQ


typealias KernelInfoInput = JPMessage<Shell.KernelInfo.Request.MetaData, Shell.KernelInfo.Request.Content>
typealias KernelInfoOutput = JPMessage<Shell.KernelInfo.Reply.MetaData, Shell.KernelInfo.Reply.Content>

class KernelInfoSender internal constructor(
    socket: ZMQ.Socket,
    msgEncoder: MsgEncoder,
    hbService: HeartBeatServiceConv,
    zContext: ZContext,
) : MsgSender<KernelInfoInput,Result<KernelInfoOutput,Exception>>{

    private val zSender = ZSender<KernelInfoInput,KernelInfoOutput>(
        socket,msgEncoder, hbService, zContext
    )
    override fun send(message: KernelInfoInput): Result<KernelInfoOutput,Exception> {
        return zSender.send<Shell.KernelInfo.Request.MetaData,Shell.KernelInfo.Request.Content>(message)
    }
}
