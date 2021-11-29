package com.github.xadkile.bicp.message.api.connection.kernel_context

import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatServiceConv
import com.github.xadkile.bicp.message.api.msg.sender.MsgSender
import com.github.xadkile.bicp.message.api.msg.sender.shell.*
import org.zeromq.ZContext

class SenderProviderImp internal constructor(
    val kernelContext:KernelContextReadOnlyConv
) :
    SenderProvider {

    override fun getExecuteRequestSender(): MsgSender<ExecuteRequest, Result<ExecuteReply, Exception>> {
        return ExecuteSender(kernelContext)
    }

    override fun getKernelInfoSender(): MsgSender<KernelInfoInput, Result<KernelInfoOutput, Exception>> {
        return KernelInfoSender(kernelContext)
    }
}
