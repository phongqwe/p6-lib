package com.github.xadkile.bicp.message.api.connection.ipython_context

import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatServiceConv
import com.github.xadkile.bicp.message.api.sender.MsgSender
import com.github.xadkile.bicp.message.api.sender.shell.ExecuteRequestInput
import com.github.xadkile.bicp.message.api.sender.shell.ExecuteRequestOutput
import com.github.xadkile.bicp.message.api.sender.shell.ExecuteRequestSender
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ

class SenderProviderImp internal constructor(
    val channelProvider: ChannelProvider,
    val zcontext: ZContext,
    val msgEncoder: MsgEncoder,
    val heartBeatServiceConv: HeartBeatServiceConv,

) :
    SenderProvider {

    private val _executeRequestSender: MsgSender<ExecuteRequestInput, Result<ExecuteRequestOutput, Exception>> by lazy {
        val socket: ZMQ.Socket = this.zcontext.createSocket(SocketType.REQ).also {
            val channelAddress = this.channelProvider.shellChannel().makeAddress()
            it.connect(channelAddress)
        }
        ExecuteRequestSender(socket, this.msgEncoder,heartBeatServiceConv,zcontext)
    }

    override fun getExecuteRequestSender(): MsgSender<ExecuteRequestInput, Result<ExecuteRequestOutput, Exception>> {
        return this._executeRequestSender
    }
}
