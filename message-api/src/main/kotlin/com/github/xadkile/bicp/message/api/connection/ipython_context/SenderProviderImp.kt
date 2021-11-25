package com.github.xadkile.bicp.message.api.connection.ipython_context

import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatServiceConv
import com.github.xadkile.bicp.message.api.msg.sender.MsgSender
import com.github.xadkile.bicp.message.api.msg.sender.shell.*
import com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition.Shell
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ

class SenderProviderImp internal constructor(
    val zcontext: ZContext,
    val msgEncoder: MsgEncoder,
    val heartBeatServiceConv: HeartBeatServiceConv,
    val socketProvider: SocketProvider
) :
    SenderProvider {

    override fun getExecuteRequestSender(): MsgSender<ExecuteRequestInput, Result<ExecuteRequestOutput, Exception>> {
        return ExecuteRequestSender(socketProvider.shellSocket(), this.msgEncoder,heartBeatServiceConv,zcontext)
    }

    override fun getKernelInfoSender(): MsgSender<KernelInfoInput, Result<KernelInfoOutput, Exception>> {
        return KernelInfoSender(socketProvider.shellSocket(), this.msgEncoder,heartBeatServiceConv,zcontext)
    }
}
