package com.github.xadkile.bicp.message.api.connection.ipython_context

import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatServiceConv
import com.github.xadkile.bicp.message.api.msg.sender.MsgSender
import com.github.xadkile.bicp.message.api.msg.sender.shell.*
import org.zeromq.ZContext

class SenderProviderImp internal constructor(
    val zcontext: ZContext,
    val msgEncoder: MsgEncoder,
    val heartBeatServiceConv: HeartBeatServiceConv,
    val socketProvider: SocketProvider
) :
    SenderProvider {

    override fun getExecuteRequestSender(): MsgSender<ExecuteReply, Result<ExecuteRequest, Exception>> {
        return ExecuteSender(socketProvider.shellSocket(), this.msgEncoder,heartBeatServiceConv,zcontext)
    }

    override fun getKernelInfoSender(): MsgSender<KernelInfoInput, Result<KernelInfoOutput, Exception>> {
        return KernelInfoSender(socketProvider.shellSocket(), this.msgEncoder,heartBeatServiceConv,zcontext)
    }
}
