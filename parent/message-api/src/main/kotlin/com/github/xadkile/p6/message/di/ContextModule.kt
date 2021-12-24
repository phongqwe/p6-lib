package com.github.xadkile.p6.message.di

import com.github.xadkile.p6.message.api.connection.kernel_context.KernelContext
import com.github.xadkile.p6.message.api.connection.kernel_context.KernelContextImp
import com.github.xadkile.p6.message.api.connection.kernel_context.KernelContextReadOnly
import dagger.Binds
import dagger.Module
import dagger.Provides
import org.zeromq.ZContext
import javax.inject.Singleton

/**
 */
@Module
interface ContextModule {
    @Binds
    fun ipythonContext(context: KernelContextImp): KernelContext

    @Binds
    fun backboneObjProvider(provider:KernelContextImp): KernelContextReadOnly

    companion object {
        @Provides
        @Singleton
        fun zContext(): ZContext {
            return ZContext()
        }
    }
}
