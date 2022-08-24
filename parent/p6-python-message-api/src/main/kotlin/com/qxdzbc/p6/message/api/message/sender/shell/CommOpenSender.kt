package com.qxdzbc.p6.message.api.message.sender.shell

import com.qxdzbc.common.error.ErrorReport
import com.qxdzbc.p6.message.api.connection.kernel_context.KernelContextReadOnly
import com.qxdzbc.p6.message.api.connection.kernel_services.KernelServiceManager
import com.qxdzbc.p6.message.api.connection.kernel_context.errors.KernelErrors
import com.qxdzbc.p6.message.api.message.protocol.JPMessage
import com.qxdzbc.p6.message.api.message.protocol.data_interface_definition.Shell
import com.qxdzbc.p6.message.api.message.sender.MsgSender
import com.qxdzbc.p6.message.api.message.sender.ZMQMsgSender
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen

typealias CommOpenRequest = JPMessage<Shell.Comm.Open.MetaData, Shell.Comm.Open.Content>

class CommOpenSender(
    private val kernelContext: KernelContextReadOnly,
    private val kernelServiceManager: KernelServiceManager,
) : MsgSender<CommOpenRequest, Result<Unit, ErrorReport>> {
    override suspend fun send(message: CommOpenRequest): Result<Unit, ErrorReport> {
        if (this.kernelContext.isKernelNotRunning()) {
            return Err(KernelErrors.KernelDown.report("CommOpenSender can't send message because the kernel is down. Message header is:\n"+
                    "${message.header}"
            ))
        }


        return kernelContext.getSocketFactory().andThen {
            val shellSocket=it.shellSocket()
            kernelContext.getMsgEncoder().andThen {
                val msgEncoder = it
                kernelServiceManager.getHeartBeatServiceRs().andThen {hbs->
                    val out: Result<Unit, ErrorReport> = ZMQMsgSender.sendJPMsgNoReply(
                        message,
                        shellSocket,
                        msgEncoder,
                        hbs
                    )
                    out
                }
            }
        }
    }
}

