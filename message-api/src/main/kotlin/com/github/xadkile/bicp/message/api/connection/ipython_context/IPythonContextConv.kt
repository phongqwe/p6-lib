package com.github.xadkile.bicp.message.api.connection.ipython_context

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.xadkile.bicp.message.api.channel.ChannelInfo
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatService
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatServiceConv
import com.github.xadkile.bicp.message.api.connection.heart_beat.HeartBeatServiceConvImp
import com.github.xadkile.bicp.message.api.protocol.KernelConnectionFileContent
import com.github.xadkile.bicp.message.api.protocol.other.MsgIdGenerator
import org.zeromq.ZContext
import java.io.InputStream
import java.io.OutputStream

interface IPythonContextConv : IPythonContext{
    fun getHeartBeatChannel():Result<ChannelInfo,Exception>{
        return this.getChannelProvider().map { it.getHeartbeatChannel() }
    }

    fun getHeartBeatAddress():Result<String,Exception>{
        return this.getHeartBeatChannel().map { it.makeAddress() }
    }

    fun getConvHeartBeatService():Result<HeartBeatServiceConv,Exception>{
        return this.getHeartBeatService().map { it.conv() }
    }

    fun original():IPythonContext

    override fun startIPython(): Result<Unit, Exception> {
        return this.original().startIPython()
    }

    override fun stopIPython(): Result<Unit, Exception> {
        return this.original().stopIPython()
    }

    override fun restartIPython(): Result<Unit, Exception> {
        return this.original().restartIPython()
    }

    override fun getIPythonProcess(): Result<Process, Exception> {
        return this.original().getIPythonProcess()
    }

    override fun getIPythonInputStream(): Result<InputStream, Exception> {
        return this.original().getIPythonInputStream()
    }

    override fun getIPythonOutputStream(): Result<OutputStream, Exception> {
        return this.original().getIPythonOutputStream()
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

    override fun setOnBeforeProcessStopListener(listener: OnIPythonContextEvent) {
        this.original().setOnBeforeProcessStopListener(listener)
    }

    override fun removeBeforeOnProcessStopListener() {
        this.original().removeBeforeOnProcessStopListener()
    }

    override fun setOnAfterProcessStopListener(listener: OnIPythonContextEvent) {
        this.original().setOnAfterProcessStopListener(listener)
    }

    override fun removeAfterOnProcessStopListener() {
        this.original().removeAfterOnProcessStopListener()
    }

    override fun setOnStartProcessListener(listener: OnIPythonContextEvent) {
        this.original().setOnStartProcessListener(listener)
    }

    override fun removeOnProcessStartListener() {
        this.original().removeOnProcessStartListener()
    }

    override fun getHeartBeatService(): Result<HeartBeatService, Exception> {
        return this.original().getHeartBeatService()
    }

    override fun conv(): IPythonContextConv {
        return this.original().conv()
    }

    override fun zContext(): ZContext {
        return this.original().zContext()
    }
}

