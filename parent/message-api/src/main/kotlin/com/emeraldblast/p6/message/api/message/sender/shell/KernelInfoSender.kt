package com.emeraldblast.p6.message.api.message.sender.shell

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.unwrap
import com.emeraldblast.p6.common.exception.error.ErrorReport
import com.emeraldblast.p6.message.api.connection.kernel_context.KernelContextReadOnly
import com.emeraldblast.p6.message.api.connection.kernel_context.context_object.MsgEncoder
import com.emeraldblast.p6.message.api.connection.kernel_context.errors.KernelErrors
import com.emeraldblast.p6.message.api.connection.service.heart_beat.HeartBeatService
import com.emeraldblast.p6.message.api.message.protocol.JPMessage
import com.emeraldblast.p6.message.api.message.protocol.data_interface_definition.Shell
import com.emeraldblast.p6.message.api.message.sender.MsgSender
import com.emeraldblast.p6.message.api.message.sender.PCSender
import org.zeromq.ZContext
import org.zeromq.ZMQ


typealias KernelInfoInput = JPMessage<Shell.KernelInfo.Request.MetaData, Shell.KernelInfo.Request.Content>
typealias KernelInfoOutput = JPMessage<Shell.KernelInfo.Reply.MetaData, Shell.KernelInfo.Reply.Content>

class KernelInfoSender internal constructor(
    val kernelContext: KernelContextReadOnly,
) : MsgSender<KernelInfoInput, Result<KernelInfoOutput, ErrorReport>> {

    override suspend fun send(
        message: KernelInfoInput,
    ): Result<KernelInfoOutput, ErrorReport> {
        if (kernelContext.isKernelNotRunning()) {
            return Err(
                ErrorReport(
                    header = KernelErrors.KernelDown.header,
                    data = KernelErrors.KernelDown.Data(""),
                    loc = "${this.javaClass.canonicalName}.send"
                )
            )
        }

        val socket: ZMQ.Socket = kernelContext.getSocketProvider().unwrap().shellSocket()
        val msgEncoder: MsgEncoder = kernelContext.getMsgEncoder().unwrap()
        val hbService: HeartBeatService = kernelContext.getHeartBeatService().unwrap()
        val zContext: ZContext = kernelContext.zContext()
        val zSender = PCSender<KernelInfoInput, KernelInfoOutput>(socket, msgEncoder, hbService, zContext)
        val rt = zSender.send2<Shell.KernelInfo.Request.MetaData, Shell.KernelInfo.Request.Content>(message)
        return rt

    }
}
