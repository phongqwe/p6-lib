package org.bitbucket.xadkile.isp.ide.jupyter.message.di

import dagger.Module
import dagger.Provides
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.channel.ChannelInfo
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.KernelConnectionFileContent
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message.data_interface_definition.Control
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message.data_interface_definition.IOPub
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message.data_interface_definition.Shell
import javax.inject.Singleton

@Module
interface ChannelModule {
    companion object {
        @JvmStatic
        @Provides
        @Singleton
        @Shell.Address
        fun shellChannelAddress(@Shell.Channel channelInfo: ChannelInfo):String{
            return channelInfo.makeAddress()
        }

        @JvmStatic
        @Provides
        @Singleton
        @Control.Address
        fun controlChannelAddress(@Control.Channel channelInfo: ChannelInfo):String{
            return channelInfo.makeAddress()
        }

        @JvmStatic
        @Provides
        @Singleton
        @IOPub.Address
        fun iopubChannelAddress(@IOPub.Channel channelInfo: ChannelInfo):String{
            return channelInfo.makeAddress()
        }


        @JvmStatic
        @Provides
        @Singleton
        @Shell.Channel
        fun shellChannel(connectFile: KernelConnectionFileContent): ChannelInfo {
            return connectFile.createShellChannel()
        }

        @JvmStatic
        @Provides
        @Singleton
        @Control.Channel
        fun controlChannel(connectFile: KernelConnectionFileContent): ChannelInfo {
            return connectFile.createControlChannel()
        }

        @JvmStatic
        @Provides
        @Singleton
        @IOPub.Channel
        fun iopubChannel(connectFile: KernelConnectionFileContent): ChannelInfo {
            return connectFile.createIOPubChannel()
        }
    }
}
