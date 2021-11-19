package com.github.xadkile.bicp.message.api.connection.ipython_context

import com.github.xadkile.bicp.message.api.channel.ChannelInfo

/**
 * provide channel info with the up-to-date connection info
 */
interface ChannelProvider {
    fun getIOPubChannel(): ChannelInfo
    fun getShellChannel(): ChannelInfo
    fun getControlChannel(): ChannelInfo
    fun getHeartbeatChannel(): ChannelInfo
}

