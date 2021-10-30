package org.bitbucket.xadkile.myide.ide.jupyter.message.di

import dagger.BindsInstance
import dagger.Component
import dagger.Subcomponent
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.channel.ChannelInfo
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.channel.IsShellChannel
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.session.Session
import org.bitbucket.xadkile.myide.ide.jupyter.message.imp.shell.ExecuteRequestSender
import javax.inject.Singleton

@Singleton
@Component(modules = [MsgIdGeneratorModule::class])
interface MessageApiComponent {
    fun shellExecuteRequestSender():ExecuteRequestSender

    @Component.Builder
    interface Builder{
        @BindsInstance
        fun session(session:Session):Builder

        @BindsInstance
        @IsShellChannel
        fun shellChannel(channel:ChannelInfo):Builder
    }
}
