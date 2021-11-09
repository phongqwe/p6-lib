package com.github.xadkile.bicp.message.api.connection

import com.github.xadkile.bicp.message.api.channel.ChannelInfo
import com.github.xadkile.bicp.message.api.protocol.KernelConnectionFileContent

class ChannelProviderImp(private val connectFile: KernelConnectionFileContent) :
    com.github.xadkile.bicp.message.api.connection.ChannelProvider {
    override fun getIOPubChannel(): com.github.xadkile.bicp.message.api.channel.ChannelInfo {
        return this.connectFile.createIOPubChannel()
    }

    override fun getShellChannel(): com.github.xadkile.bicp.message.api.channel.ChannelInfo {
        return this.connectFile.createShellChannel()
    }

    override fun getControlChannel(): com.github.xadkile.bicp.message.api.channel.ChannelInfo {
        return this.connectFile.createControlChannel()
    }

}
