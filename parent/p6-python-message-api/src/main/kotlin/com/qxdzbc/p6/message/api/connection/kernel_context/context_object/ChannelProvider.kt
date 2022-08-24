package com.qxdzbc.p6.message.api.connection.kernel_context.context_object

/**
 * provide channel info with the up-to-date connection info
 */
interface ChannelProvider {
    fun ioPubChannel(): ChannelInfo
    fun shellChannel(): ChannelInfo
    fun controlChannel(): ChannelInfo
    fun heartbeatChannel(): ChannelInfo

    fun ioPubAddress():String
    fun heartBeatAddress():String
    fun controlAddress():String
    fun shellAddress():String

}

