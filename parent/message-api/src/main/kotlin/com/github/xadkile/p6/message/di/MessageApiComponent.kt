package com.github.xadkile.p6.message.di

import com.github.xadkile.p6.message.api.connection.kernel_context.ApplicationCoroutineScope
import com.github.xadkile.p6.message.api.connection.kernel_context.KernelConfig
import com.github.xadkile.p6.message.api.connection.kernel_context.KernelContext
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import org.zeromq.ZContext
import javax.inject.Singleton


@Component(modules = [KernelContextModule::class])
interface MessageApiComponent {

    fun zContext(): ZContext
    fun kernelContext(): KernelContext

    @Component.Builder
    interface Builder{
        fun build():MessageApiComponent

        @BindsInstance
        fun kernelConfig(config: KernelConfig):Builder

        @BindsInstance
        fun applicationCoroutineScope( @ApplicationCoroutineScope scope:CoroutineScope):Builder

        @BindsInstance
        fun networkServiceCoroutineDispatcher(dispatcher:CoroutineDispatcher):Builder
    }
}
