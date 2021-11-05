package org.bitbucket.xadkile.isp.ide.jupyter.message.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.other.MsgCounterImp
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.other.MsgIdGenerator
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.protocol.other.SequentialMsgIdGenerator
import org.bitbucket.xadkile.isp.ide.jupyter.message.api.connection.SessionUUID
import org.bitbucket.xadkile.myide.ide.jupyter.message.api.protocol.message.MsgCounter
import javax.inject.Named
import javax.inject.Singleton

/**
 * House misc classes
 */
@Module
interface OtherModule {

    @Binds
    abstract fun bindMsgCounter(counter: MsgCounterImp): MsgCounter

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

    }
}
