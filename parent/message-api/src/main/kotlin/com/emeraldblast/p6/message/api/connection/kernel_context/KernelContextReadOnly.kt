package com.emeraldblast.p6.message.api.connection.kernel_context

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.emeraldblast.p6.common.exception.error.ErrorReport
import com.emeraldblast.p6.message.api.connection.kernel_context.context_object.ChannelInfo
import com.emeraldblast.p6.message.api.connection.kernel_context.context_object.*
import com.emeraldblast.p6.message.api.connection.kernel_context.context_object.ChannelProvider
import com.emeraldblast.p6.message.api.connection.service.heart_beat.HeartBeatService
import com.emeraldblast.p6.message.api.connection.service.iopub.IOPubListenerService
import com.emeraldblast.p6.message.api.connection.service.zmq_services.ZMQListenerService
import com.emeraldblast.p6.message.api.connection.service.zmq_services.msg.P6Message
import com.emeraldblast.p6.message.api.connection.service.zmq_services.msg.P6Response
import com.emeraldblast.p6.message.api.message.protocol.KernelConnectionFileContent
import com.emeraldblast.p6.message.api.message.protocol.other.MsgIdGenerator
import org.zeromq.ZContext

/**
 * Limiting interface that provide read-only access to context objects.
 *
 * This is for preventing changing kernel context state by mistake
 */
interface KernelContextReadOnly {


    val kernelConfig: KernelConfig?


    /**
     * Return content of connection file .
     *
     * Connection file is available for use only when IPython process is launch successfully.
     */
    fun getConnectionFileContent(): Result<KernelConnectionFileContent, ErrorReport>

    fun getSession(): Result<Session, ErrorReport>

    fun getChannelProvider(): Result<ChannelProvider, ErrorReport>

    fun getSenderProvider(): Result<SenderProvider, ErrorReport>

    fun getMsgEncoder(): Result<MsgEncoder, ErrorReport>

    fun getMsgIdGenerator(): Result<MsgIdGenerator, ErrorReport>

    fun getSocketProvider():Result<SocketFactory, ErrorReport>

    fun zContext(): ZContext

    /**
     * when a kernel is in "running" state, kernel process and all context-related objects are running and not null
     */
    fun isKernelRunning():Boolean

    /**
     * A running context guarantees that all context-related objects and services are up, running, not null
     */
    fun isAllRunning():Boolean

    /**
     * A stopped context guarantees that all context-related objects and services are stopped and null
     */
    fun isKernelNotRunning():Boolean

    fun getHeartBeatChannel(): Result<ChannelInfo, ErrorReport> {
        return this.getChannelProvider().map { it.heartbeatChannel() }
    }

    fun getHeartBeatAddress(): Result<String, ErrorReport> {
        return this.getHeartBeatChannel().map { it.makeAddress() }
    }

    fun getShellChannel():Result<ChannelInfo, ErrorReport>{
        return this.getChannelProvider().map { it.shellChannel() }
    }

    fun getShellAddress():Result<String, ErrorReport>{
        return this.getChannelProvider().map { it.shellAddress() }
    }

    fun getIOPubChannel():Result<ChannelInfo, ErrorReport>{
        return this.getChannelProvider().map { it.ioPubChannel() }
    }

    fun getIOPubAddress():Result<String, ErrorReport>{
        return this.getChannelProvider().map { it.ioPubAddress() }
    }

    fun getControlChannel():Result<ChannelInfo, ErrorReport>{
        return this.getChannelProvider().map { it.controlChannel() }
    }

    fun getControlAddress():Result<String, ErrorReport>{
        return this.getChannelProvider().map { it.controlAddress() }
    }
}

