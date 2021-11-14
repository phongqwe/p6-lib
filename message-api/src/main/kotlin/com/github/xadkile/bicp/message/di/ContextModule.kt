package com.github.xadkile.bicp.message.di

import com.github.xadkile.bicp.message.api.connection.IPythonContext
import com.github.xadkile.bicp.message.api.connection.IPythonContextImp
import dagger.Binds
import dagger.Module

/**
 */
@Module
interface ContextModule {
    @Binds
    fun ipythonContext(context:IPythonContextImp):IPythonContext
}
