package com.github.xadkile.bicp.message.di

import dagger.Module
import dagger.Provides
import com.github.xadkile.bicp.message.api.connection.SessionInfo
import com.github.xadkile.bicp.message.api.connection.SessionUUID

@Module
interface SessionModule {
    companion object {
        @JvmStatic
        @Provides
        @SessionUUID
        fun sessionUUID(session: SessionInfo):String{
            return session.sessionId
        }

    }
}
