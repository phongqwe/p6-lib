package com.github.xadkile.bicp.message.api.connection.ipython_context

import com.github.michaelbull.result.Result
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatService
import com.github.xadkile.bicp.message.api.msg.protocol.KernelConnectionFileContent
import com.github.xadkile.bicp.message.api.msg.protocol.other.MsgIdGenerator
import org.zeromq.ZContext

/**
 * Limiting interface to safely access context-bound objects.
 *
 * This is for preventing mistakenly changing IPython context state, such as calling start, stop in a sender.
 */
interface IPythonContextReadOnly {
    /**
     * Return content of connection file .
     *
     * Connection file is available for use only when IPython process is launch successfully.
     */
    fun getConnectionFileContent(): Result<com.github.xadkile.bicp.message.api.msg.protocol.KernelConnectionFileContent, Exception>

    fun getSession(): Result<Session, Exception>

    fun getChannelProvider(): Result<ChannelProvider, Exception>

    fun getSenderProvider(): Result<SenderProvider, Exception>

    fun getMsgEncoder(): Result<MsgEncoder, Exception>

    fun getMsgIdGenerator(): Result<MsgIdGenerator, Exception>

    fun getHeartBeatService():Result<HeartBeatService,Exception>

    fun getSocketProvider():Result<SocketProvider,Exception>

    fun zContext(): ZContext
    /**
     * convert this to a more convenient but more dangerous to use interface
     */
    fun conv():IPythonContextReadOnlyConv

    /**
     * A running context guarantees that all context-related objects and services are up, running, not null
     */
    fun isRunning():Boolean

    /**
     * A stopped context guarantees that all context-related objects and services are stopped and null
     */
    fun isNotRunning():Boolean

}

