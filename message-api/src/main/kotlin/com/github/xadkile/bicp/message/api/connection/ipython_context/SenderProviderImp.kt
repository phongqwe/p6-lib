package com.github.xadkile.bicp.message.api.connection.ipython_context

import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.sender.MsgSender
import com.github.xadkile.bicp.message.api.sender.shell.ExecuteRequestInput
import com.github.xadkile.bicp.message.api.sender.shell.ExecuteRequestOutput
import com.github.xadkile.bicp.message.api.sender.shell.ExecuteRequestSender
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ

class SenderProviderImp internal constructor(val channelProvider: ChannelProvider, val zcontext: ZContext, val msgEncoder: MsgEncoder) :
    SenderProvider {

    private val executeRequestSender: MsgSender<ExecuteRequestInput, Result<ExecuteRequestOutput, Exception>> by lazy{
        this.getNewExecuteRequestSender()
    }
    override fun getSingletonExecuteRequestSender(): MsgSender<ExecuteRequestInput, Result<ExecuteRequestOutput, Exception>> {
        return this.executeRequestSender
    }

    override fun getNewExecuteRequestSender(): MsgSender<ExecuteRequestInput, Result<ExecuteRequestOutput, Exception>> {
        val socket: ZMQ.Socket = this.zcontext.createSocket(SocketType.REQ).also {
            val channelAddress = this.channelProvider.getShellChannel().makeAddress()
            it.connect(channelAddress)
        }
        return ExecuteRequestSender(socket, msgEncoder)
    }
}
