package com.qxdzbc.p6.message.api.message.sender.shell

import com.qxdzbc.common.error.ErrorReport
import com.qxdzbc.p6.message.api.connection.kernel_context.KernelContextReadOnly
import com.qxdzbc.p6.message.api.connection.kernel_services.KernelServiceManager
import com.qxdzbc.p6.message.api.connection.kernel_context.errors.KernelErrors
import com.qxdzbc.p6.message.api.message.protocol.JPMessage
import com.qxdzbc.p6.message.api.message.protocol.data_interface_definition.Shell
import com.qxdzbc.p6.message.api.message.sender.PCSender
import com.github.michaelbull.result.*
import org.zeromq.ZContext
import javax.inject.Inject


typealias KernelInfoInput = JPMessage<Shell.KernelInfo.Request.MetaData, Shell.KernelInfo.Request.Content>
typealias KernelInfoOutput = JPMessage<Shell.KernelInfo.Reply.MetaData, Shell.KernelInfo.Reply.Content>

class KernelInfoSenderImp @Inject constructor(
    val kernelContext: KernelContextReadOnly,
    private val kernelServiceManager: KernelServiceManager,
) : KernelInfoSender {

    override suspend fun send(
        message: KernelInfoInput,
    ): Result<KernelInfoOutput, ErrorReport> {
        if (kernelContext.isKernelNotRunning()) {
            return Err(
                KernelErrors.KernelDown.report("${this::class.simpleName} can't send message because the kernel is down. Message header:\n" + "${message.header}")
            )
        }

        val rt = kernelContext.getSocketFactory().map { it.shellSocket() }
            .andThen { socket ->
                kernelContext.getMsgEncoder().andThen { msgEncoder ->
                    kernelServiceManager.getHeartBeatServiceRs().andThen { hbService ->
                        val zContext: ZContext = kernelContext.zContext()
                        val zSender =
                            PCSender<KernelInfoInput, KernelInfoOutput>(socket, msgEncoder, hbService, zContext)
                        val o =
                            zSender.send<Shell.KernelInfo.Request.MetaData, Shell.KernelInfo.Request.Content>(message)
                        o
                    }
                }
            }
        return rt
//        val socket: ZMQ.Socket = kernelContext.getSocketProvider().unwrap().shellSocket()
//        val msgEncoder: MsgEncoder = kernelContext.getMsgEncoder().unwrap()
//        val hbService: HeartBeatService = kernelContext.getHeartBeatService().unwrap()
//        val zContext: ZContext = kernelContext.zContext()
//        val zSender = PCSender<KernelInfoInput, KernelInfoOutput>(socket, msgEncoder, hbService, zContext)
//        val rt = zSender.send2<Shell.KernelInfo.Request.MetaData, Shell.KernelInfo.Request.Content>(message)
//        return rt

    }
}
