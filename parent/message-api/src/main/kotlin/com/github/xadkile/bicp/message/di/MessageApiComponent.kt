package com.github.xadkile.bicp.message.di

import com.github.xadkile.bicp.message.api.connection.kernel_context.ApplicationCScope
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelConfig
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelContext
import com.github.xadkile.bicp.message.api.connection.kernel_context.KernelTimeOut
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import org.zeromq.ZContext
import javax.inject.Singleton


@Singleton
@Component(modules = [ContextModule::class])
interface MessageApiComponent {
    fun zContext(): ZContext
    fun ipythonContext(): KernelContext

    @Component.Builder
    interface Builder{
        fun build():MessageApiComponent

        @BindsInstance
        fun kernelConfig(config: KernelConfig):Builder

        @BindsInstance
        fun applicationCoroutineScope( @ApplicationCScope scope:CoroutineScope):Builder

        @BindsInstance
        fun networkServiceCoroutineDispatcher(dispatcher:CoroutineDispatcher):Builder

        @BindsInstance
        fun kernelTimeOut(kernelTimeOut: KernelTimeOut):Builder
    }
}
