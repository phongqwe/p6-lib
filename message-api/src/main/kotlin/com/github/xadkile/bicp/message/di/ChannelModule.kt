package com.github.xadkile.bicp.message.di

import dagger.Module
import dagger.Provides
import com.github.xadkile.bicp.message.api.channel.ChannelInfo
import com.github.xadkile.bicp.message.api.protocol.KernelConnectionFileContent
import com.github.xadkile.bicp.message.api.protocol.message.data_interface_definition.Control
import com.github.xadkile.bicp.message.api.protocol.message.data_interface_definition.IOPub
import com.github.xadkile.bicp.message.api.protocol.message.data_interface_definition.Shell
import javax.inject.Singleton

@Module
interface ChannelModule {
    companion object {
        @JvmStatic
        @Provides
        @Singleton
        @Shell.Address
        fun shellChannelAddress(@Shell.Channel channelInfo: com.github.xadkile.bicp.message.api.channel.ChannelInfo):String{
            return channelInfo.makeAddress()
        }

        @JvmStatic
        @Provides
        @Singleton
        @Control.Address
        fun controlChannelAddress(@Control.Channel channelInfo: com.github.xadkile.bicp.message.api.channel.ChannelInfo):String{
            return channelInfo.makeAddress()
        }

        @JvmStatic
        @Provides
        @Singleton
        @IOPub.Address
        fun iopubChannelAddress(@IOPub.Channel channelInfo: com.github.xadkile.bicp.message.api.channel.ChannelInfo):String{
            return channelInfo.makeAddress()
        }


        @JvmStatic
        @Provides
        @Singleton
        @Shell.Channel
        fun shellChannel(connectFile: KernelConnectionFileContent): com.github.xadkile.bicp.message.api.channel.ChannelInfo {
            return connectFile.createShellChannel()
        }

        @JvmStatic
        @Provides
        @Singleton
        @Control.Channel
        fun controlChannel(connectFile: KernelConnectionFileContent): com.github.xadkile.bicp.message.api.channel.ChannelInfo {
            return connectFile.createControlChannel()
        }

        @JvmStatic
        @Provides
        @Singleton
        @IOPub.Channel
        fun iopubChannel(connectFile: KernelConnectionFileContent): com.github.xadkile.bicp.message.api.channel.ChannelInfo {
            return connectFile.createIOPubChannel()
        }
    }
}
