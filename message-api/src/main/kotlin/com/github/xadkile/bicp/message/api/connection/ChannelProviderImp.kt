package com.github.xadkile.bicp.message.api.connection

import com.github.xadkile.bicp.message.api.channel.ChannelInfo
import com.github.xadkile.bicp.message.api.protocol.KernelConnectionFileContent

class ChannelProviderImp internal constructor(private val connectFile: KernelConnectionFileContent) :
    ChannelProvider {
    override fun getIOPubChannel(): ChannelInfo {
        return this.connectFile.createIOPubChannel()
    }

    override fun getShellChannel(): ChannelInfo {
        return this.connectFile.createShellChannel()
    }

    override fun getControlChannel(): ChannelInfo {
        return this.connectFile.createControlChannel()
    }
}
