package com.github.xadkile.p6.message.di

import com.github.xadkile.p6.message.api.connection.kernel_context.KernelContext
import com.github.xadkile.p6.message.api.connection.kernel_context.KernelContextImp
import dagger.Binds
import dagger.Module
import dagger.Provides
import org.zeromq.ZContext
import javax.inject.Singleton

/**
 */
@Module
interface KernelContextModule {
    @Binds
    @Singleton
    fun kernelContext(context: KernelContextImp): KernelContext

    companion object {
        @Provides
        @Singleton
        fun zContext(): ZContext {
            return ZContext()
        }
    }
}
