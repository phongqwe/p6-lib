package org.bitbucket.xadkile.myide.ide.jupyter.message.di

import dagger.Module
import dagger.Provides
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgCounter
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.utils.MsgIdGenerator
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.utils.SequentialMsgIdGenerator
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.session.Session
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.session.SessionUUID
import javax.inject.Named
import javax.inject.Singleton




@Module
abstract class MsgIdGeneratorModule {
    companion object {
        @JvmStatic
        @Provides
        @Singleton
        @Named("sequential")
        fun sequentialIdGenerator(
            @SessionUUID uuid:String,
            msgCounter: MsgCounter
        ): MsgIdGenerator {
            return SequentialMsgIdGenerator(uuid,msgCounter)
        }
        @JvmStatic
        @Provides
        @SessionUUID
        fun sessionUUID(session: Session):String{
            return session.sessionId
        }
    }
}
