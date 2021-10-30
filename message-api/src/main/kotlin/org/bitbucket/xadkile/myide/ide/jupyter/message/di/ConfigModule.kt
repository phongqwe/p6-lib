package org.bitbucket.xadkile.myide.ide.jupyter.message.di

import dagger.Module
import dagger.Provides
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.channel.ChannelInfo
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.channel.IsShellAddress
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.channel.IsShellChannel
import org.zeromq.ZContext
import org.zeromq.ZMQ
import javax.inject.Singleton

@Module
class ConfigModule {
    companion object {
        @JvmStatic
        @Provides
        @Singleton
        fun zeroMQContext():ZContext{
            return ZContext()
        }

        @JvmStatic
        @Provides
        @Singleton
        @IsShellAddress
        fun shellChannelAddress(
            @IsShellChannel channelInfo:ChannelInfo
        ):String{
            return channelInfo.makeAddress()
        }
    }
}
