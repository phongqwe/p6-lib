package com.emeraldblast.p6.message.di

import com.emeraldblast.p6.message.api.connection.kernel_context.KernelContext
import com.emeraldblast.p6.message.api.connection.kernel_context.KernelContextImp
import dagger.Binds
import dagger.Module
import dagger.Provides
import org.zeromq.ZContext
import javax.inject.Singleton


@Module
interface KernelContextModule {
    @Binds
    fun kernelContext(context: KernelContextImp): KernelContext

    companion object {
        @Provides
        fun zContext(): ZContext {
            return ZContext()
        }
    }
}
