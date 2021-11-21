package com.github.xadkile.bicp.message.api.connection.ipython_context

import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatServiceConv
import com.github.xadkile.bicp.message.api.msg.sender.MsgSender
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteRequestInput
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteRequestOutput
import com.github.xadkile.bicp.message.api.msg.sender.shell.ExecuteRequestSender
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

    private val _executeRequestSender: MsgSender<ExecuteRequestInput, Result<ExecuteRequestOutput, Exception>> by lazy {
        ExecuteRequestSender(socketProvider.shellSocket(), this.msgEncoder,heartBeatServiceConv,zcontext)
    }

    override fun getExecuteRequestSender(): MsgSender<ExecuteRequestInput, Result<ExecuteRequestOutput, Exception>> {
        return this._executeRequestSender
    }
}
