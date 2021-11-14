package com.github.xadkile.bicp.message.api.connection

/**
 * provide channel info with the up-to-date connection info
 */
interface ChannelProvider {
    fun getIOPubChannel(): com.github.xadkile.bicp.message.api.channel.ChannelInfo
    fun getShellChannel(): com.github.xadkile.bicp.message.api.channel.ChannelInfo
    fun getControlChannel(): com.github.xadkile.bicp.message.api.channel.ChannelInfo
}

