package org.bitbucket.xadkile.myide.ide.jupyter.message.di

import dagger.BindsInstance
import dagger.Component
import dagger.Subcomponent
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.channel.ChannelInfo
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.channel.IsShellChannel
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.session.Session
import org.bitbucket.xadkile.myide.ide.jupyter.message.imp.shell.ExecuteRequestSender
import org.zeromq.ZContext
import javax.inject.Singleton

@Singleton
@Component(modules = [MsgIdGeneratorModule::class,ConfigModule::class])
interface MessageApiComponent {
    fun shellExecuteRequestSender():ExecuteRequestSender

    @Component.Builder
    interface Builder{
        fun build():MessageApiComponent

        @BindsInstance
        fun session(session:Session):Builder

        @BindsInstance
        fun shellChannel(@IsShellChannel channel:ChannelInfo):Builder

        @BindsInstance
        fun zContext(context:ZContext):Builder

    }
}
