package org.bitbucket.xadkile.isp.ide.jupyter.message.api.connection

import org.bitbucket.xadkile.isp.ide.jupyter.message.api.channel.ChannelInfo

/**
 * provide channel info with the up-to-date connection info
 */
interface ChannelProvider {
    fun getIOPubChannel(): ChannelInfo
    fun getShellChannel(): ChannelInfo
    fun getControlChannel(): ChannelInfo
}

