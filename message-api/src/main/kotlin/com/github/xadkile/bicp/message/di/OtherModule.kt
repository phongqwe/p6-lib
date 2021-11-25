package com.github.xadkile.bicp.message.di

import dagger.Module
import dagger.Provides
import org.zeromq.ZContext
import javax.inject.Singleton

@Module
interface OtherModule {
    companion object {
        @Provides
        @Singleton
        fun zContext():ZContext{
            return ZContext()
        }
    }
}
