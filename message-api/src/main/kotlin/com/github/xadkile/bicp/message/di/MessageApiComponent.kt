package com.github.xadkile.bicp.message.di

import dagger.BindsInstance
import dagger.Component
import com.github.xadkile.bicp.message.api.protocol.KernelConnectionFileContent
import com.github.xadkile.bicp.message.api.protocol.message.data_interface_definition.IOPub
import com.github.xadkile.bicp.message.api.connection.SessionInfo
import org.zeromq.ZContext
import javax.inject.Singleton



@Singleton
@Component(modules = [OtherModule::class,ContextModule::class, ChannelModule::class])
interface MessageApiComponent {
    fun zContext(): ZContext
    fun connectionFile(): KernelConnectionFileContent

    @IOPub.Channel
    fun iopubChannel(): com.github.xadkile.bicp.message.api.channel.ChannelInfo

    @Component.Builder
    interface Builder{
        fun build():MessageApiComponent

        @BindsInstance
        fun zContext(context:ZContext):Builder

        @BindsInstance
        fun connectionFile(connectionFile:KernelConnectionFileContent):Builder

    }
}
