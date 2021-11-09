package org.bitbucket.xadkile.isp.ide.jupyter.message.di

import dagger.BindsInstance
import dagger.Component
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.channel.ChannelInfo
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.KernelConnectionFileContent
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.message.data_interface_definition.IOPub
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.connection.SessionInfo
import org.bitbucket.xadkile.isp.ide.jupyter.message.imp.shell.ExecuteRequestSender
import org.zeromq.ZContext
import javax.inject.Singleton



@Singleton
@Component(modules = [OtherModule::class,SessionModule::class, ChannelModule::class])
interface MessageApiComponent {
    fun shellExecuteRequestSender(): ExecuteRequestSender
    fun zContext(): ZContext
    fun session(): SessionInfo
    fun connectionFile(): KernelConnectionFileContent

    @IOPub.Channel
    fun iopubChannel(): ChannelInfo

    @Component.Builder
    interface Builder{
        fun build():MessageApiComponent

        @BindsInstance
        fun session(session:SessionInfo):Builder

        @BindsInstance
        fun zContext(context:ZContext):Builder

        @BindsInstance
        fun connectionFile(connectionFile:KernelConnectionFileContent):Builder

    }
}
