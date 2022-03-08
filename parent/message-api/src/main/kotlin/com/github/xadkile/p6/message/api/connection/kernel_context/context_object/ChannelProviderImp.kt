package com.github.xadkile.p6.message.api.connection.kernel_context.context_object

import com.github.xadkile.p6.message.api.message.protocol.KernelConnectionFileContent

class ChannelProviderImp internal constructor(private val connectFile: KernelConnectionFileContent) :
    ChannelProvider {

    private val ioPub: ChannelInfo by lazy {
        this.connectFile.createIOPubChannel()
    }

    override fun ioPubChannel(): ChannelInfo {
        return this.ioPub
    }

    private val shell: ChannelInfo by lazy{
        this.connectFile.createShellChannel()
    }
    override fun shellChannel(): ChannelInfo {
        return this.shell
    }

    private val control: ChannelInfo by lazy{
        this.connectFile.createControlChannel()
    }
    override fun controlChannel(): ChannelInfo {
        return control
    }

    private val hb : ChannelInfo by lazy{
        this.connectFile.createHeartBeatChannel()
    }
    override fun heartbeatChannel(): ChannelInfo {
        return this.hb
    }

    override fun ioPubAddress(): String {
        return this.ioPub.makeAddress()
    }

    override fun heartBeatAddress(): String {
        return this.hb.makeAddress()
    }

    override fun controlAddress(): String {
        return this.control.makeAddress()
    }

    override fun shellAddress(): String {
        return this.shell.makeAddress()
    }
}
