package com.emeraldblast.p6.message.di

import com.emeraldblast.p6.message.api.connection.kernel_context.KernelConfig
import com.emeraldblast.p6.message.api.connection.kernel_context.KernelContext
import com.emeraldblast.p6.message.api.connection.kernel_context.KernelContextImp
import com.emeraldblast.p6.message.api.connection.kernel_context.context_object.ChannelProvider
import com.emeraldblast.p6.message.api.connection.kernel_context.context_object.ChannelProviderImp
import com.emeraldblast.p6.message.api.connection.kernel_context.context_object.SocketFactory
import com.emeraldblast.p6.message.api.connection.kernel_context.context_object.SocketFactoryImp
import com.emeraldblast.p6.message.api.message.protocol.KernelConnectionFileContent
import com.github.michaelbull.result.unwrap
import dagger.Binds
import dagger.Module
import dagger.Provides
import org.zeromq.ZContext
import javax.inject.Singleton


@Module
interface KernelContextModule {
    @Binds
    fun kernelContext(context: KernelContextImp): KernelContext

//    @Binds
//    fun SocketFactory(i: SocketFactoryImp): SocketFactory
//
//    @Binds
//    fun ChannelProvider(i: ChannelProviderImp):ChannelProvider

    companion object {
        @Provides
        fun zContext(): ZContext {
            return ZContext()
        }

//        @Provides
//        fun KernelConnectionFileContent(kernelConfig:KernelConfig): KernelConnectionFileContent{
//            return kernelConfig.kernelConnectionFileContent!!
//        }
    }
}
