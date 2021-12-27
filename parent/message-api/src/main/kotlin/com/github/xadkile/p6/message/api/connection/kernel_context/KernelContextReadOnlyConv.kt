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


    fun getHeartBeatChannel(): Result<ChannelInfo, Exception> {
        return this.getChannelProvider().map { it.heartbeatChannel() }
    }

    fun getHeartBeatAddress(): Result<String, Exception> {
        return this.getHeartBeatChannel().map { it.makeAddress() }
    }

    fun getShellChannel():Result<ChannelInfo,Exception>{
        return this.getChannelProvider().map { it.shellChannel() }
    }

    fun getShellAddress():Result<String,Exception>{
        return this.getChannelProvider().map { it.shellAddress() }
    }

    fun getIOPubChannel():Result<ChannelInfo,Exception>{
        return this.getChannelProvider().map { it.ioPubChannel() }
    }

    fun getIOPubAddress():Result<String,Exception>{
        return this.getChannelProvider().map { it.ioPubAddress() }
    }

    fun getControlChannel():Result<ChannelInfo,Exception>{
        return this.getChannelProvider().map { it.controlChannel() }
    }

    fun getControlAddress():Result<String,Exception>{
        return this.getChannelProvider().map { it.controlAddress() }
    }

    fun original(): KernelContextReadOnly


    // === OVERLOADED === //


    override fun getKernelConfig(): KernelConfig {
        return original().getKernelConfig()
    }

    override fun getIOPubListenerService2(): Result<IOPubListenerServiceReadOnly, ErrorReport> {
        return original().getIOPubListenerService2()
    }

    override fun getConnectionFileContent2(): Result<KernelConnectionFileContent, ErrorReport> {
        return original().getConnectionFileContent2()
    }

    override fun getSession2(): Result<Session, ErrorReport> {
        return original().getSession2()
    }

    override fun getChannelProvider2(): Result<ChannelProvider, ErrorReport> {
        return original().getChannelProvider2()
    }

    override fun getSenderProvider2(): Result<SenderProvider, ErrorReport> {
        return original().getSenderProvider2()
    }

    override fun getMsgEncoder2(): Result<MsgEncoder, ErrorReport> {
        return original().getMsgEncoder2()
    }

    override fun getMsgIdGenerator2(): Result<MsgIdGenerator, ErrorReport> {
        return original().getMsgIdGenerator2()
    }

    override fun getHeartBeatService2(): Result<HeartBeatService, ErrorReport> {
        return original().getHeartBeatService2()
    }

    override fun getSocketProvider2(): Result<SocketProvider, ErrorReport> {
        return original().getSocketProvider2()
    }

    override fun areServicesRunning(): Boolean {
        return original().areServicesRunning()
    }

    override fun isAllRunning(): Boolean {
        return original().isAllRunning()
    }

    override fun getIOPubListenerService(): Result<IOPubListenerServiceReadOnly, Exception> {
        return this.original().getIOPubListenerService()
    }

    override fun getSocketProvider(): Result<SocketProvider, Exception> {
        return this.original().getSocketProvider()
    }

    override fun getConnectionFileContent(): Result<KernelConnectionFileContent, Exception> {
        return this.original().getConnectionFileContent()
    }

    override fun getSession(): Result<Session, Exception> {
        return this.original().getSession()
    }

    override fun getChannelProvider(): Result<ChannelProvider, Exception> {
        return this.original().getChannelProvider()
    }

    override fun getSenderProvider(): Result<SenderProvider, Exception> {
        return this.original().getSenderProvider()
    }

    override fun getMsgEncoder(): Result<MsgEncoder, Exception> {
        return this.original().getMsgEncoder()
    }

    override fun getMsgIdGenerator(): Result<MsgIdGenerator, Exception> {
        return this.original().getMsgIdGenerator()
    }

    override fun getHeartBeatService(): Result<HeartBeatService, Exception> {
        return this.original().getHeartBeatService()
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

