package com.github.xadkile.bicp.message.api.msg.sender.shell

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.unwrap
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatServiceConv
import com.github.xadkile.bicp.message.api.connection.ipython_context.KernelContext
import com.github.xadkile.bicp.message.api.connection.ipython_context.KernelContextReadOnlyConv
import com.github.xadkile.bicp.message.api.connection.ipython_context.MsgEncoder
import com.github.xadkile.bicp.message.api.connection.ipython_context.SocketProvider
import com.github.xadkile.bicp.message.api.msg.protocol.message.JPMessage
import com.github.xadkile.bicp.message.api.msg.protocol.message.data_interface_definition.Shell
import com.github.xadkile.bicp.message.api.msg.sender.MsgSender
import com.github.xadkile.bicp.message.api.msg.sender.ZSender
import org.zeromq.ZContext
import org.zeromq.ZMQ

typealias ExecuteReply = JPMessage<Shell.Execute.Reply.MetaData, Shell.Execute.Reply.Content>

typealias ExecuteRequest = JPMessage<Shell.Execute.Request.MetaData, Shell.Execute.Request.Content>

/**
 * [zContext] is for creating poller
 *
 * If I integrate IOPub status in this sender, I violate the single-responsiblity principle, because this sender only handle sending execute request and see if such request was delivered or not. Its reponsibility does not include handling the computation result. That's the res of IOpUb listener.
 * If I want to do send execute request, then get execute result, then I must create a third class to fuse IOPub and this sender together.
 */
class ExecuteSender internal constructor(
    val kernelContext: KernelContextReadOnlyConv,
) : MsgSender<ExecuteRequest,
        Result<ExecuteReply, Exception>> {
    override fun send(message: ExecuteRequest): Result<ExecuteReply, Exception> {
        val zsender = ZSender<ExecuteRequest, ExecuteReply>(
            kernelContext.getSocketProvider().unwrap().shellSocket(),
            kernelContext.getMsgEncoder().unwrap(),
            kernelContext.getConvHeartBeatService().unwrap(),
            kernelContext.zContext())
        val rt = zsender.send<Shell.Execute.Reply.MetaData, Shell.Execute.Reply.Content>(message)
        return rt
    }
}
