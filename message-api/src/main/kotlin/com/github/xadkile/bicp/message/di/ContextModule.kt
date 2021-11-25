package com.github.xadkile.bicp.message.di

import com.github.xadkile.bicp.message.api.connection.ipython_context.IPythonContextReadOnly
import com.github.xadkile.bicp.message.api.connection.ipython_context.IPythonContext
import com.github.xadkile.bicp.message.api.connection.ipython_context.IPythonContextImp
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
    fun ipythonContext(context: IPythonContextImp): IPythonContext

    @Binds
    fun backboneObjProvider(provider:IPythonContextImp): IPythonContextReadOnly
    companion object {
        @Provides
        @Singleton
        fun zContext(): ZContext {
            return ZContext()
        }
    }
}
