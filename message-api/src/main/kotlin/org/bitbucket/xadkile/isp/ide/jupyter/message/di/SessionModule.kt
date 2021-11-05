package org.bitbucket.xadkile.isp.ide.jupyter.message.di

import dagger.Module
import dagger.Provides
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.connection.SessionInfo
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.connection.SessionUUID

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
