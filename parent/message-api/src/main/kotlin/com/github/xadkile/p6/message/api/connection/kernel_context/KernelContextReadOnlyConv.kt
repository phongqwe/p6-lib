package com.github.xadkile.p6.message.api.connection.kernel_context

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.xadkile.p6.exception.error.ErrorReport
import com.github.xadkile.p6.message.api.channel.ChannelInfo
import com.github.xadkile.p6.message.api.connection.kernel_context.context_object.*
import com.github.xadkile.p6.message.api.connection.service.heart_beat.HeartBeatService
import com.github.xadkile.p6.message.api.connection.service.iopub.IOPubListenerServiceReadOnly
import com.github.xadkile.p6.message.api.msg.protocol.KernelConnectionFileContent
import com.github.xadkile.p6.message.api.msg.protocol.other.MsgIdGenerator
import org.zeromq.ZContext

/**
 * A more convenient KernelContext interface
 */
interface KernelContextReadOnlyConv : KernelContextReadOnly {

    fun getHeartBeatChannel(): Result<ChannelInfo, ErrorReport> {
        return this.getChannelProvider().map { it.heartbeatChannel() }
    }

    fun getHeartBeatAddress(): Result<String, ErrorReport> {
        return this.getHeartBeatChannel().map { it.makeAddress() }
    }

    fun getShellChannel():Result<ChannelInfo,ErrorReport>{
        return this.getChannelProvider().map { it.shellChannel() }
    }

    fun getShellAddress():Result<String,ErrorReport>{
        return this.getChannelProvider().map { it.shellAddress() }
    }

    fun getIOPubChannel():Result<ChannelInfo,ErrorReport>{
        return this.getChannelProvider().map { it.ioPubChannel() }
    }

    fun getIOPubAddress():Result<String,ErrorReport>{
        return this.getChannelProvider().map { it.ioPubAddress() }
    }

    fun getControlChannel():Result<ChannelInfo,ErrorReport>{
        return this.getChannelProvider().map { it.controlChannel() }
    }

    fun getControlAddress():Result<String,ErrorReport>{
        return this.getChannelProvider().map { it.controlAddress() }
    }

    fun original(): KernelContextReadOnly


    // === OVERLOADED === //


    override fun getKernelConfig(): KernelConfig {
        return original().getKernelConfig()
    }

    override fun getIOPubListenerService(): Result<IOPubListenerServiceReadOnly, ErrorReport> {
        return original().getIOPubListenerService()
    }

    override fun getConnectionFileContent(): Result<KernelConnectionFileContent, ErrorReport> {
        return original().getConnectionFileContent()
    }

    override fun getSession(): Result<Session, ErrorReport> {
        return original().getSession()
    }

    override fun getChannelProvider(): Result<ChannelProvider, ErrorReport> {
        return original().getChannelProvider()
    }

    override fun getSenderProvider(): Result<SenderProvider, ErrorReport> {
        return original().getSenderProvider()
    }

    override fun getMsgEncoder(): Result<MsgEncoder, ErrorReport> {
        return original().getMsgEncoder()
    }

    override fun getMsgIdGenerator(): Result<MsgIdGenerator, ErrorReport> {
        return original().getMsgIdGenerator()
    }

    override fun getHeartBeatService(): Result<HeartBeatService, ErrorReport> {
        return original().getHeartBeatService()
    }

    override fun getSocketProvider(): Result<SocketProvider, ErrorReport> {
        return original().getSocketProvider()
    }

    override fun areServicesRunning(): Boolean {
        return original().areServicesRunning()
    }

    override fun isAllRunning(): Boolean {
        return original().isAllRunning()
    }

    override fun zContext(): ZContext {
        return this.original().zContext()
    }

    override fun isKernelRunning(): Boolean {
        return this.original().isKernelRunning()
    }

    override fun isKernelNotRunning(): Boolean {
        return this.original().isKernelNotRunning()
    }
}

