package org.bitbucket.xadkile.isp.ide.jupyter.message.api.connection

import org.bitbucket.xadkile.isp.ide.jupyter.message.api.channel.ChannelInfo
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.KernelConnectionFileContent

class ChannelProviderImp(private val connectFile: KernelConnectionFileContent) : ChannelProvider {
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
