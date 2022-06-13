package com.emeraldblast.p6.message.di

import com.emeraldblast.p6.message.api.connection.kernel_context.KernelConfig
import com.emeraldblast.p6.message.api.connection.kernel_context.KernelContext
import com.emeraldblast.p6.message.api.connection.kernel_context.KernelContextImp
import com.emeraldblast.p6.message.api.connection.kernel_context.KernelContextReadOnly
import com.emeraldblast.p6.message.api.connection.kernel_context.context_object.ChannelProvider
import com.emeraldblast.p6.message.api.connection.kernel_context.context_object.ChannelProviderImp
import com.emeraldblast.p6.message.api.connection.kernel_context.context_object.SocketFactory
import com.emeraldblast.p6.message.api.connection.kernel_context.context_object.SocketFactoryImp
import com.emeraldblast.p6.message.api.message.protocol.KernelConnectionFileContent
import com.emeraldblast.p6.message.api.message.protocol.other.MsgCounterImp
import com.emeraldblast.p6.message.api.message.protocol.other.MsgIdGenerator
import com.emeraldblast.p6.message.api.message.protocol.other.RandomMsgIdGenerator
import com.github.michaelbull.result.unwrap
import dagger.Binds
import dagger.Module
import dagger.Provides
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgCounter
import org.zeromq.ZContext
import java.util.*
import javax.inject.Singleton


@Module
interface KernelContextModule {
    @Binds
    fun kernelContext(context: KernelContextImp): KernelContext

    @Binds
    fun KernelContextReadOnly(context: KernelContextImp): KernelContextReadOnly

    @Binds
    fun msgCounter(i: MsgCounterImp): MsgCounter

    @Binds
    fun MsgIdGenerator(i: RandomMsgIdGenerator):MsgIdGenerator

    companion object {

        @Provides
        fun zContext(): ZContext {
            return ZContext()
        }

        @Provides
        @SessionId
        @MsgApiScope
        fun sessionId():String{
            return UUID.randomUUID().toString()
        }

        @Provides
        @SystemUsername
        @MsgApiScope
        fun systemUsername():String{
            return System.getProperty("user.name")
        }
    }
}
