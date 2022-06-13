package com.emeraldblast.p6.message.di

import com.emeraldblast.p6.message.api.connection.kernel_context.KernelConfig
import com.emeraldblast.p6.message.api.connection.kernel_context.KernelContext
import com.emeraldblast.p6.message.api.connection.kernel_context.KernelContextImp
import com.emeraldblast.p6.message.api.connection.kernel_context.KernelContextReadOnly
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
    @MsgApiScope
    fun kernelContext(context: KernelContextImp): KernelContext

    @Binds
    @MsgApiScope
    fun KernelContextReadOnly(context: KernelContext): KernelContextReadOnly

    @Binds
    @MsgApiScope
    fun msgCounter(i: MsgCounterImp): MsgCounter

    @Binds
    @MsgApiScope
    fun MsgIdGenerator(i: RandomMsgIdGenerator):MsgIdGenerator

    companion object {

        @Provides
        @MsgApiScope
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
