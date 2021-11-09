package com.github.xadkile.bicp.message.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import com.github.xadkile.bicp.message.api.protocol.other.MsgCounterImp
import com.github.xadkile.bicp.message.api.protocol.other.MsgIdGenerator
import com.github.xadkile.bicp.message.api.protocol.other.SequentialMsgIdGenerator
import com.github.xadkile.bicp.message.api.connection.SessionUUID
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
